package com.my.junit.testmanager.services;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.my.junit.testmanager.config.TestGeneratorConfig;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Сервис для генерации классов-генераторов тестовых данных для заданных классов.
 * Обеспечивает рекурсивный сбор зависимостей, генерацию статических фабричных методов
 * и интеграцию с конфигурацией (TestGeneratorConfig).
 */
public class TestDataGenerator {
    private static final String JAVA_EXTENSION = ".java";
    private static final String IMPORT_STATIC_PREFIX = "import static ";
    private static final String SEMICOLON = ";";
    private static final String TARGET_TYPE_CLASS = "CLASS";
    private static final String TARGET_TYPE_METHOD = "METHOD";
    private static final String PUBLIC_CLASS_PREFIX = "public class ";
    private static final String CLASS_OPEN_BRACE = " {\n";
    private static final String CLASS_CLOSE_BRACE = "}\n";
    private static final String METHOD_INDENT = "    ";
    private static final String NULL_VALUE = "null";

    private final LoggerUtils log;
    private final Map<String, PsiClass> generatedGenerators;
    private final Project project;
    private final TestGeneratorConfig config;

    /**
     * Создает новый экземпляр генератора.
     *
     * @param project Проект IntelliJ IDEA.
     */
    public TestDataGenerator(@NotNull Project project) {
        this.project = project;
        this.config = TestGeneratorConfig.getInstance(project);
        this.log = LoggerUtils.getLogger(TestDataGenerator.class);
        this.generatedGenerators = new HashMap<>();
    }

    /**
     * Генерирует генератор данных для заданного класса и всех его зависимостей.
     * Рекурсивно обрабатывает зависимости, избегая дубликатов.
     *
     * @param clazz Основной класс для генерации.
     */
    public void generateGeneratorForClass(@NotNull PsiClass clazz) {
        final var module = ModuleUtilCore.findModuleForPsiElement(clazz);
        if (module == null) {
            log.logWarn("No module found for class: " + clazz.getName());
            return;
        }

        final var dependents = collectAllDependentClasses(clazz, new HashSet<>());
        for (var dep : dependents) {
            generateGeneratorForClass(dep);
        }

        generateSingleGenerator(clazz, module);
    }

    /**
     * Генерирует одиночный генератор для класса в модуле.
     * Включает подготовку контента, создание файла и логирование.
     */
    private void generateSingleGenerator(@NotNull PsiClass clazz, @NotNull Module module) {
        final var className = clazz.getName();
        final var generatorName = getGeneratorClassName(clazz);
        final var packageName = PsiUtils.extractPackageFromFqcn(requireNonNull(clazz.getQualifiedName()));

        if (isGeneratorAlreadyExists(packageName, generatorName, module)) {
            log.logInfo("Generator already exists, skipping: " + generatorName);
            return;
        }

        final Set<PsiClass> classesForThisGenerator = collectClassesForGenerator(clazz);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                final var sourceTestRoot = findOrCreateSourceTestRoot(module);
                if (sourceTestRoot == null) {
                    log.logWarn("Source root not found for module: " + module.getName());
                    return;
                }

                final var sourcePackageDir = findOrCreatePackageDir(project, sourceTestRoot, packageName);
                if (sourcePackageDir == null) {
                    log.logWarn("Source package directory could not be found or created for package: " + packageName);
                    return;
                }

                final var content = prepareGeneratorContent(generatorName, classesForThisGenerator);
                final var javaFile = createJavaFile(generatorName, packageName, content);
                final var createdFile = addFileToDirectory(sourcePackageDir, javaFile, packageName, generatorName);
                log.logInfo("Generated generator class: " + generatorName + " in package: " + packageName);
                
                // Обновляем индексы и рефакторинг после создания файла
                if (createdFile != null) {
                    refreshFileIndices(createdFile);
                }
            } catch (Exception e) {
                log.logError("Failed to generate generator for class: " + className, e);
            }
        });
    }

    /**
     * Подготавливает контент генератора: текст класса, импорты и методы.
     *
     * @param generatorName Имя генерируемого класса.
     * @param classes       Набор классов для генерации методов.
     * @return Структура с готовым контентом.
     */
    @NotNull
    private GeneratorContent prepareGeneratorContent(@NotNull String generatorName, @NotNull Set<PsiClass> classes) {
        final var usedImports = new HashSet<String>();
        final var classBuilder = new StringBuilder();
        final var importsBuilder = new StringBuilder();

        appendClassAnnotations(classBuilder, usedImports);
        appendClassDeclaration(classBuilder, generatorName);
        appendMethods(classBuilder, usedImports, classes);
        closeClassDeclaration(classBuilder);
        buildImports(importsBuilder, usedImports);

        return new GeneratorContent(importsBuilder.toString(), classBuilder.toString());
    }

    /**
     * Собирает все классы для генератора (основной + вложенные).
     */
    @NotNull
    private Set<PsiClass> collectClassesForGenerator(@NotNull PsiClass clazz) {
        final var classes = new HashSet<PsiClass>();
        classes.add(clazz);
        classes.addAll(Arrays.asList(clazz.getInnerClasses()));
        return classes;
    }

    /**
     * Проверяет, существует ли уже генератор.
     */
    private boolean isGeneratorAlreadyExists(
            @NotNull String packageName,
            @NotNull String generatorName,
            @NotNull Module module
    ) {
        return JavaPsiFacade.getInstance(project)
                .findClass(packageName + "." + generatorName, GlobalSearchScope.moduleScope(module)) != null;
    }

    /**
     * Добавляет файл в директорию и регистрирует сгенерированный класс.
     * 
     * @return Созданный PsiJavaFile или null в случае ошибки
     */
    @Nullable
    private PsiJavaFile addFileToDirectory(
            @NotNull PsiDirectory dir,
            @NotNull PsiJavaFile file,
            @NotNull String packageName,
            @NotNull String generatorName
    ) {
        try {
            final var finalFile = (PsiJavaFile) dir.add(file);
            final var classes = finalFile.getClasses();
            if (classes.length > 0) {
                generatedGenerators.put(packageName + "." + generatorName, classes[0]);
            }
            return finalFile;
        } catch (Exception e) {
            log.logError("Failed to add file to directory: " + dir.getVirtualFile().getPath(), e);
            return null;
        }
    }

    /**
     * Обновляет индексы проекта для созданного файла.
     * Помечает файл как измененный и обновляет PSI кэш.
     */
    private void refreshFileIndices(@NotNull PsiJavaFile file) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                final var virtualFile = file.getVirtualFile();
                if (virtualFile != null) {
                    // Помечаем файл как измененный для обновления индексов
                    VfsUtil.markDirtyAndRefresh(false, false, false, virtualFile);
                    // Обновляем PSI кэш
                    PsiManager.getInstance(project).dropPsiCaches();
                    log.logInfo("Refreshed indices for file: " + virtualFile.getPath());
                }
            } catch (Exception e) {
                log.logError("Failed to refresh file indices", e);
            }
        });
    }

    /**
     * Создает PsiJavaFile из контента.
     */
    @NotNull
    private PsiJavaFile createJavaFile(
            @NotNull String generatorName,
            @NotNull String packageName,
            @NotNull GeneratorContent content
    ) {
        final var fileText = buildFullFileText(packageName, content);
        return (PsiJavaFile) PsiFileFactory.getInstance(project)
                .createFileFromText(generatorName + JAVA_EXTENSION, JavaFileType.INSTANCE, fileText);
    }

    /**
     * Собирает полный текст файла (package + imports + class).
     */
    @NotNull
    private String buildFullFileText(@NotNull String packageName, @NotNull GeneratorContent content) {
        return "package " + packageName + ";\n\n" + content.imports() + "\n" + content.classText();
    }

    /**
     * Рекурсивно собирает зависимости класса (на основе полей, включая записи record).
     * Использует visited для избежания циклов.
     * Для record'ов обрабатывает все поля компонентов.
     *
     * @param clazz   Основной класс.
     * @param visited Набор уже посещенных классов.
     * @return Набор зависимых классов.
     */
    @NotNull
    private Set<PsiClass> collectAllDependentClasses(@Nullable PsiClass clazz, @NotNull Set<PsiClass> visited) {
        if (clazz == null || visited.contains(clazz)) {
            return new HashSet<>();
        }
        visited.add(clazz);

        final var dependents = new HashSet<PsiClass>();
        
        // Собираем прямые зависимости
        if (PsiUtils.isRecord(clazz)) {
            dependents.addAll(collectRecordDependencies(clazz));
        } else {
            dependents.addAll(collectFieldDependencies(clazz));
        }
        
        // Собираем косвенные зависимости (рекурсивно)
        final var indirectDependents = collectIndirectDependencies(dependents, visited);
        dependents.addAll(indirectDependents);
        
        // Собираем зависимости из вложенных классов
        final var innerDependents = collectInnerClassDependencies(clazz, visited);
        dependents.addAll(innerDependents);

        return dependents;
    }

    /**
     * Собирает зависимости из компонентов record'а.
     */
    @NotNull
    private Set<PsiClass> collectRecordDependencies(@NotNull PsiClass recordClass) {
        return Arrays.stream(recordClass.getMethods())
                .filter(method -> method.getParameterList().isEmpty() && !method.isConstructor())
                .map(PsiMethod::getReturnType)
                .filter(Objects::nonNull)
                .filter(PsiClassType.class::isInstance)
                .map(type -> ((PsiClassType) type).resolve())
                .filter(Objects::nonNull)
                .filter(resolved -> !resolved.equals(recordClass))
                .collect(Collectors.toSet());
    }

    /**
     * Собирает зависимости из полей обычного класса.
     */
    @NotNull
    private Set<PsiClass> collectFieldDependencies(@NotNull PsiClass clazz) {
        return Arrays.stream(clazz.getFields())
                .filter(field -> field.getType() instanceof PsiClassType)
                .map(field -> ((PsiClassType) field.getType()).resolve())
                .filter(Objects::nonNull)
                .filter(resolved -> !resolved.equals(clazz))
                .collect(Collectors.toSet());
    }

    /**
     * Рекурсивно собирает косвенные зависимости (зависимости зависимостей).
     */
    @NotNull
    private Set<PsiClass> collectIndirectDependencies(
            @NotNull Set<PsiClass> directDependents,
            @NotNull Set<PsiClass> visited
    ) {
        final var indirectDependents = new HashSet<PsiClass>();
        for (var dependent : directDependents) {
            indirectDependents.addAll(collectAllDependentClasses(dependent, visited));
        }
        return indirectDependents;
    }

    /**
     * Собирает зависимости из вложенных классов.
     */
    @NotNull
    private Set<PsiClass> collectInnerClassDependencies(@NotNull PsiClass clazz, @NotNull Set<PsiClass> visited) {
        final var innerDependents = new HashSet<PsiClass>();
        for (var inner : clazz.getInnerClasses()) {
            innerDependents.addAll(collectAllDependentClasses(inner, visited));
        }
        return innerDependents;
    }

    /**
     * Генерирует методы для списка классов.
     */
    private void appendMethods(
            @NotNull StringBuilder classBuilder,
            @NotNull Set<String> usedImports,
            @NotNull Set<PsiClass> classes
    ) {
        classes.stream()
                .filter(Objects::nonNull)
                .filter(clazz -> clazz.getName() != null)
                .forEach(clazz -> appendMethod(classBuilder, usedImports, clazz));
    }

    /**
     * Генерирует текст одного статического метода для класса.
     */
    private void appendMethod(
            @NotNull StringBuilder classBuilder,
            @NotNull Set<String> usedImports,
            @NotNull PsiClass clazz
    ) {
        appendMethodAnnotations(classBuilder, usedImports);
        appendMethodBody(classBuilder, usedImports, clazz);
    }

    /**
     * Добавляет аннотацию на класс (из конфига).
     */
    private void appendClassAnnotations(@NotNull StringBuilder builder, @NotNull Set<String> usedImports) {
        config.getAnnotationsDataList().stream()
                .filter(annotation -> TARGET_TYPE_CLASS.equals(annotation.getTargetType()))
                .forEach(annotation -> {
                    builder.append(annotation.getAnnotationText()).append("\n");
                    if (annotation.getImports() != null && !annotation.getImports().isEmpty()) {
                        usedImports.add(annotation.getImports());
                    }
                });
    }

    /**
     * Добавляет начало объявления класса.
     *
     * @param builder       StringBuilder для appending.
     * @param generatorName Имя генератора.
     */
    private void appendClassDeclaration(@NotNull StringBuilder builder, @NotNull String generatorName) {
        builder.append(PUBLIC_CLASS_PREFIX).append(generatorName).append(CLASS_OPEN_BRACE);
    }

    /**
     * Добавляет конец объявления класса.
     */
    private void closeClassDeclaration(@NotNull StringBuilder builder) {
        builder.append(CLASS_CLOSE_BRACE);
    }

    /**
     * Добавляет аннотацию на метод (из конфига).
     */
    private void appendMethodAnnotations(@NotNull StringBuilder builder, @NotNull Set<String> usedImports) {
        config.getAnnotationsDataList().stream()
                .filter(annotation -> TARGET_TYPE_METHOD.equals(annotation.getTargetType()))
                .forEach(annotation -> {
                    builder.append(METHOD_INDENT).append(annotation.getAnnotationText()).append("\n");
                    if (annotation.getImports() != null && !annotation.getImports().isEmpty()) {
                        usedImports.add(annotation.getImports());
                    }
                });
    }

    /**
     * Добавляет тело метода (с параметрами из полей или компонентов record'а).
     */
    private void appendMethodBody(
            @NotNull StringBuilder builder,
            @NotNull Set<String> usedImports,
            @NotNull PsiClass clazz
    ) {
        final var className = clazz.getName();
        final var methodName = config.getGeneratorMethodName() + className;
        final var paramsText = buildMethodParameters(usedImports, clazz);
        final var methodText = buildMethodText(className, methodName, paramsText);
        
        builder.append(methodText);
        log.logInfo("Added method text for: " + methodName);
    }

    /**
     * Строит строку параметров для конструктора на основе типа класса (record или обычный класс).
     */
    @NotNull
    private String buildMethodParameters(@NotNull Set<String> usedImports, @NotNull PsiClass clazz) {
        final var paramsBuilder = new StringBuilder("\n");
        
        if (PsiUtils.isRecord(clazz)) {
            appendRecordParameters(paramsBuilder, usedImports, clazz);
        } else {
            appendClassFieldParameters(paramsBuilder, usedImports, clazz);
        }
        return paramsBuilder.toString().trim();
    }

    /**
     * Добавляет параметры для record (из компонентов record'а).
     */
    private void appendRecordParameters(
            @NotNull StringBuilder paramsBuilder,
            @NotNull Set<String> usedImports,
            @NotNull PsiClass clazz
    ) {
        final var methods = clazz.getMethods();
        int methodIndex = 0;
        for (var method : methods) {
            if (method.getParameterList().isEmpty() && !method.isConstructor()) {
                final var componentType = method.getReturnType();
                if (componentType != null) {
                    final var paramValue = generateValueForType(componentType, usedImports, clazz);
                    paramsBuilder.append("                ").append(paramValue);
                    paramsBuilder.append(methodIndex < methods.length - 1 ? ",\n" : "\n");
                    methodIndex++;
                }
            }
        }
    }

    /**
     * Добавляет параметры для обычного класса (из полей класса).
     */
    private void appendClassFieldParameters(
            @NotNull StringBuilder paramsBuilder,
            @NotNull Set<String> usedImports,
            @NotNull PsiClass clazz
    ) {
        final var fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            final var field = fields[i];
            final var paramValue = generateValueForType(field.getType(), usedImports, clazz);
            paramsBuilder.append("                ").append(paramValue);
            paramsBuilder.append(i < fields.length - 1 ? ",\n" : "\n");
        }
    }

    /**
     * Строит полный текст метода генератора.
     */
    @NotNull
    private String buildMethodText(@NotNull String className, @NotNull String methodName, @NotNull String paramsText) {
        final var formattedParams = paramsText.isEmpty()
                ? ""
                : "\n" + paramsText + "\n            ";
        return String.format(
                "    public static %s %s() {\n        return new %s(%s);\n    }\n",
                className,
                methodName,
                className,
                formattedParams
        );
    }

    /**
     * Генерирует значение для типа поля/компонента.
     * Использует фабрики из конфига или генераторы для пользовательских классов.
     * Если ничего не найдено, возвращает null.
     */
    @NotNull
    private String generateValueForType(
            @NotNull PsiType type,
            @NotNull Set<String> usedImports,
            @NotNull PsiClass clazz
    ) {
        final var canonicalTypeName = type.getCanonicalText();

        // Проверяем фабрики из конфига
        final var factory = config.findFactoryMethodForType(canonicalTypeName);
        if (factory != null) {
            usedImports.add(factory.getImports());
            return factory.getMethodName();
        }

        // Обработка пользовательских классов
        if (type instanceof PsiClassType psiClassType) {
            final var resolved = psiClassType.resolve();
            if (resolved != null && !resolved.equals(clazz)) {
                final var fqcn = resolved.getQualifiedName();
                if (fqcn != null) {
                    final var typePackage = PsiUtils.extractPackageFromFqcn(fqcn);
                    final var typeName = resolved.getName();
                    final var generatorClassName = typeName + config.getGeneratorClassName();
                    final var generateName = config.getGeneratorMethodName() + typeName;
                    final var importStr =
                            IMPORT_STATIC_PREFIX
                                    + typePackage
                                    + "."
                                    + generatorClassName
                                    + "."
                                    + generateName
                                    + SEMICOLON;
                    usedImports.add(importStr);
                    return generateName + "()";
                }
            }
        }
        
        return NULL_VALUE;
    }

    /**
     * Строит строку импортов.
     */
    private void buildImports(@NotNull StringBuilder builder, @NotNull Set<String> usedImports) {
        usedImports.stream()
                .sorted()
                .forEach(imp -> builder.append(imp).append("\n"));
    }

    /**
     * Находит или создает корень тестового источника.
     * Пытается найти существующую тестовую директорию, если не найдена - создает её.
     */
    @Nullable
    private VirtualFile findOrCreateSourceTestRoot(@NotNull Module module) {
        final var moduleRootManager = ModuleRootManager.getInstance(module);
        var sourceRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE);
        
        if (!sourceRoots.isEmpty()) {
            final var testRoot = sourceRoots.get(0);
            if (testRoot.exists() && testRoot.isDirectory()) {
                log.logInfo("Found existing test source root: " + testRoot.getPath());
                return testRoot;
            }
        }

        // Пытаемся найти main source root и создать рядом test директорию
        final var mainRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.SOURCE);
        if (!mainRoots.isEmpty()) {
            final var mainRoot = mainRoots.get(0);
            final var mainRootPath = mainRoot.getPath();
            
            // Пытаемся найти стандартные тестовые директории
            final var possibleTestPaths = new String[]{
                    mainRootPath.replace("/src/main/java", "/src/test/java"),
                    mainRootPath.replace("/src/main/java", "/src/test"),
                    mainRootPath.replace("src/main/java", "src/test/java"),
                    mainRootPath.replace("src/main/java", "src/test"),
                    mainRootPath + "/../test/java",
                    mainRootPath + "/../test"
            };

            for (var testPath : possibleTestPaths) {
                final var testDir = VfsUtil.findFileByIoFile(new java.io.File(testPath), false);
                if (testDir != null && testDir.exists() && testDir.isDirectory()) {
                    log.logInfo("Found test directory at: " + testDir.getPath());
                    return testDir;
                }
            }

            // Если не нашли, пытаемся создать test/java рядом с main/java
            try {
                final var mainRootParent = mainRoot.getParent();
                if (mainRootParent != null) {
                    var testDir = mainRootParent.findChild("test");
                    if (testDir == null || !testDir.isDirectory()) {
                        testDir = mainRootParent.createChildDirectory(null, "test");
                        log.logInfo("Created test directory: " + testDir.getPath());
                    }
                    
                    var testJavaDir = testDir.findChild("java");
                    if (testJavaDir == null || !testJavaDir.isDirectory()) {
                        testJavaDir = testDir.createChildDirectory(null, "java");
                        log.logInfo("Created test/java directory: " + testJavaDir.getPath());
                    }
                    
                    return testJavaDir;
                }
            } catch (Exception e) {
                log.logError("Failed to create test directory structure", e);
            }

            log.logWarn("Test source root not found, using main source root for tests: " + mainRoot.getPath());
            return mainRoot;
        }
        
        log.logError("No source roots found for module: " + module.getName());
        return null;
    }

    /**
     * Находит или создает директорию пакета, создавая поддиректории рекурсивно.
     *
     * @param project     Проект.
     * @param root        Корень.
     * @param packageName Имя пакета (например, "com.example.test").
     * @return PsiDirectory или null в случае ошибки.
     */
    @Nullable
    private PsiDirectory findOrCreatePackageDir(
            @NotNull Project project,
            @NotNull VirtualFile root,
            @Nullable String packageName
    ) {
        var currentDir = root;
        if (packageName == null || packageName.trim().isEmpty()) {
            log.logInfo("Using default package, root directory: " + currentDir.getPath());
            return PsiManager.getInstance(project).findDirectory(currentDir);
        }

        final var parts = packageName.split("\\.");
        for (var part : parts) {
            if (part.trim().isEmpty()) {
                continue;
            }
            var subDir = currentDir.findChild(part);
            if (subDir == null || !subDir.isDirectory()) {
                try {
                    subDir = currentDir.createChildDirectory(null, part);
                    log.logInfo("Created subdirectory: " + part + " in " + currentDir.getPath());
                } catch (Exception e) {
                    log.logError("Failed to create subdirectory '" + part + "' in " + currentDir.getPath(), e);
                    return null;
                }
            }
            currentDir = subDir;
        }

        final var psiDir = PsiManager.getInstance(project).findDirectory(currentDir);
        if (psiDir == null) {
            log.logWarn("PsiDirectory not found for created VirtualFile: " + currentDir.getPath());
        }
        return psiDir;
    }

    /**
     * Возвращает имя генерируемого класса-генератора.
     */
    @NotNull
    private String getGeneratorClassName(@NotNull PsiClass clazz) {
        return clazz.getName() + this.config.getGeneratorClassName();
    }

    /**
     * Внутренняя запись для хранения контента генератора.
     */
    private record GeneratorContent(@NotNull String imports, @NotNull String classText) {}
}



