package com.my.junit.testmanager.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.JavaRefactoringFactory;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Утилитарный класс для работы с PSI элементами.
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PsiUtils {
    private static final LoggerUtils log = LoggerUtils.getLogger(PsiUtils.class);

    /**
     * Поиск всех PsiClass по имени класса в проекте.
     *
     * @param project   проект IntelliJ IDEA.
     * @param className имя класса для поиска.
     * @return список найденных PsiClass.
     */
    @NotNull
    public static List<PsiClass> findClassesByName(@NotNull Project project, @NotNull String className) {
        final var cache = PsiShortNamesCache.getInstance(project);
        final var allClasses = cache.getClassesByName(className, GlobalSearchScope.allScope(project));
        return Arrays.stream(allClasses)
                .toList();
    }


    /**
     * Получение всех изменённых PsiClass из всех модулей проекта для указанного типа исходного кода.
     *
     * @param project        проект IntelliJ IDEA.
     * @param sourceRootType тип исходного кода (SOURCE или TEST_SOURCE).
     * @return список изменённых PsiClass.
     */
    @NotNull
    public static List<PsiClass> getChangedPsiClassesFromAllModules(
            @NotNull Project project,
            @NotNull JavaSourceRootType sourceRootType
    ) {
        final var psiClasses = new ArrayList<PsiClass>();
        final var changeListManager = ChangeListManager.getInstance(project);
        final var changes = changeListManager.getAllChanges();
        final var psiManager = PsiManager.getInstance(project);
        final var projectFileIndex = ProjectFileIndex.getInstance(project);

        for (var change : changes) {
            final var afterRevision = change.getAfterRevision();
            if (afterRevision != null) {

                final var virtualFile = afterRevision.getFile().getVirtualFile();
                if (virtualFile != null && virtualFile.isValid()) {

                    final var module = projectFileIndex.getModuleForFile(virtualFile);
                    if (module == null) {
                        continue;
                    }

                    final var rootManager = ModuleRootManager.getInstance(module);
                    final var relevantSourceRoots = rootManager.getSourceRoots(sourceRootType);
                    if (relevantSourceRoots.isEmpty()) {
                        continue;
                    }

                    final var psiFile = psiManager.findFile(virtualFile);
                    if (psiFile instanceof PsiJavaFile javaFile) {
                        psiClasses.addAll(List.of(javaFile.getClasses()));
                    }
                }
            }
        }
        return psiClasses;
    }

    /**
     * Получение всех PsiClass из всех модулей проекта для указанного типа исходного кода.
     *
     * @param project        проект IntelliJ IDEA.
     * @param sourceRootType тип исходного кода (SOURCE или TEST_SOURCE).
     * @return список всех PsiClass.
     */
    @NotNull
    public static List<PsiClass> getAllPsiClassesFromAllModules(
            @NotNull Project project,
            @NotNull JavaSourceRootType sourceRootType
    ) {
        final var psiClasses = new ArrayList<PsiClass>();
        final var moduleManager = ModuleManager.getInstance(project);

        for (var module : moduleManager.getModules()) {
            final var rootManager = ModuleRootManager.getInstance(module);
            for (var root : rootManager.getSourceRoots(sourceRootType)) {
                final var psiDirectory = PsiManager.getInstance(project).findDirectory(root);
                if (psiDirectory != null) {
                    psiClasses.addAll(
                            getAllPsiClassesFromDirectory(psiDirectory));
                }
            }
        }
        return psiClasses;
    }

    /**
     * Получение всех PsiClass из указанной директории.
     *
     * @param project   проект IntelliJ IDEA.
     * @param directory директория VirtualFile.
     * @return список всех PsiClass.
     */
    @NotNull
    public static List<PsiClass> getAllPsiClassesFromDirectory(
            @NotNull Project project,
            @NotNull VirtualFile directory
    ) {
        final var psiManager = PsiManager.getInstance(project);
        final var psiDirectory = psiManager.findDirectory(directory);
        if (psiDirectory != null) {
            return getAllPsiClassesFromDirectory(psiDirectory);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Получение всех PsiClass из указанной PSI директории.
     *
     * @param directory директория PsiDirectory.
     * @return список всех PsiClass.
     */
    @NotNull
    public static List<PsiClass> getAllPsiClassesFromDirectory(@NotNull PsiDirectory directory) {
        final var psiClasses = new ArrayList<PsiClass>();

        for (var file : directory.getFiles()) {
            if (file instanceof PsiJavaFile javaFile) {
                psiClasses.addAll(List.of(javaFile.getClasses()));
            }
        }

        // Рекурсивный вызов для поддиректорий
        for (var subDir : directory.getSubdirectories()) {
            psiClasses.addAll(getAllPsiClassesFromDirectory(subDir));
        }

        return psiClasses;
    }

    /**
     * Проверка, находится ли указанный PsiClass в корне тестовых исходников.
     *
     * @param psiClass PsiClass для проверки.
     * @param project  проект IntelliJ IDEA.
     * @return true, если PsiClass находится в корне тестовых исходников, иначе false.
     */
    public static boolean isInTestSourceRoot(@NotNull PsiClass psiClass, @NotNull Project project) {
        final var file = psiClass.getContainingFile();
        if (file == null) {
            return false;
        }

        final var virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }

        final var module = ModuleUtilCore.findModuleForFile(virtualFile, project);
        if (module == null) {
            return false;
        }

        final var testSourceRoots =
                ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.TEST_SOURCE);

        for (var root : testSourceRoots) {
            if (VfsUtilCore.isAncestor(root, virtualFile, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверка, совпадают ли пакеты у двух FQCN.
     *
     * @param classFqcn Класс FQCN.
     * @param testFqcn  Тестовый FQCN.
     * @return true, если пакеты совпадают, иначе false.
     */
    public static boolean isPackageMatching(@NotNull String classFqcn, @NotNull String testFqcn) {
        final var classPackage = extractPackageFromFqcn(classFqcn);
        final var testPackage = extractPackageFromFqcn(testFqcn);
        log.logInfo("Comparing packages - Class: " + classPackage + ", Test: " + testPackage);
        return Objects.equals(classPackage, testPackage);
    }

    /**
     * Вычисление нового FQCN для теста при несовпадении пакетов.
     *
     * @param classFqcn Класс FQCN.
     * @param testName  Имя теста.
     * @return Новый FQCN для теста.
     */
    @NotNull
    public static String calculateNewTestFqcnForMismatch(@NotNull String classFqcn, @NotNull String testName) {
        log.logInfo("Calculating new FQCN for test: " + testName + " based on class FQCN: " + classFqcn);
        final var classPackage = extractPackageFromFqcn(classFqcn);
        return classPackage.isEmpty() ? testName : classPackage;
    }

    /**
     * Извлечение пакета из полного имени класса (FQCN).
     *
     * @param fqcn Полное имя класса (FQCN).
     * @return Имя пакета или пустая строка, если пакет отсутствует.
     */
    @NotNull
    public static String extractPackageFromFqcn(@NotNull String fqcn) {
        int lastDot = fqcn.lastIndexOf('.');
        return lastDot > 0 ? fqcn.substring(0, lastDot) : "";
    }

    /**
     * Проверка, есть ли импорт targetClass в testClass.
     *
     * @param testClass   Тестовый класс.
     * @param targetClass Целевой класс.
     * @return true, если импорт присутствует, иначе false.
     */
    public static boolean hasImportOnClass(@NotNull PsiClass testClass, @NotNull PsiClass targetClass) {
        final var file = testClass.getContainingFile();
        if (!(file instanceof PsiJavaFile javaFile)) {
            return false;
        }

        final var importList = javaFile.getImportList();
        if (importList == null) {
            return false;
        }

        final var targetQualifiedName = targetClass.getQualifiedName();
        if (targetQualifiedName == null) {
            return false;
        }

        for (var importStmt : importList.getImportStatements()) {
            if (importStmt.getQualifiedName() != null &&
                    importStmt.getQualifiedName().equals(targetQualifiedName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверка, является ли указанный PsiClass тестовым классом.
     *
     * @param psiClass PsiClass для проверки.
     * @return true, если PsiClass является тестовым классом, иначе false.
     */
    public static boolean isTestClass(@NotNull PsiClass psiClass) {

        boolean hasTestAnnotation = PsiTreeUtil.findChildrenOfType(psiClass, PsiAnnotation.class)
                .stream()
                .anyMatch(annotation -> {
                    final var qualifiedName = annotation.getQualifiedName();
                    return (qualifiedName != null && qualifiedName.contains("Test"));
                });

        boolean inheritsFromTestCase = Stream.of(psiClass.getSupers())
                .anyMatch(superClass -> "junit.framework.TestCase".equals(superClass.getQualifiedName()));

        boolean nameEndsWithTest = psiClass.getName() != null && psiClass.getName().endsWith("Test");

        return hasTestAnnotation || inheritsFromTestCase || nameEndsWithTest;
    }

    /**
     * Открытие редактора файла для указанного PsiClass.
     *
     * @param project  проект IntelliJ IDEA.
     * @param psiClass PsiClass для открытия в редакторе.
     */
    public static void openPsiClassForEditor(
            @NotNull Project project,
            @NotNull PsiClass psiClass
    ) {
        log.logInfo("Opening file editor for class: " + psiClass.getQualifiedName());
        try {
            FileEditorManager.getInstance(project)
                    .openFile(
                            psiClass.getContainingFile()
                                    .getVirtualFile(),
                            true
                    );
        } catch (Exception e) {
            MessagesDialogUtils.messageError(
                    project,
                    MessagesBundle.message(
                            "dialog.error.open.file",
                            psiClass.getQualifiedName()
                    )
            );
        }
    }

    /**
     * Перемещение указанного PsiClass в целевой пакет.
     *
     * @param project       проект IntelliJ IDEA.
     * @param element       PsiElement (класс) для перемещения.
     * @param targetPackage целевой пакет для перемещения.
     */
    public static void movePsiClass(
            @NotNull Project project,
            @NotNull PsiElement element,
            @NotNull String targetPackage
    ) {
        final var refactoringFactory = JavaRefactoringFactory.getInstance(project);
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                final var moveDestination =
                        refactoringFactory.createSourceFolderPreservingMoveDestination(targetPackage);
                final var refactoring = refactoringFactory.createMoveClassesOrPackages(
                        new PsiElement[]{ element },
                        moveDestination,
                        false,
                        true
                );
                refactoring.run();
                log.logInfo("Successfully moved class to package: " + targetPackage);
            } catch (Exception ex) {
                log.logError("Error during move refactoring to package: " + targetPackage, ex);
            }
        });
    }

    /**
     * Проверяет, является ли заданный PsiClass record'ом (с Java 14+).
     *
     * @param clazz PsiClass для проверки.
     * @return true, если класс — record, иначе false.
     */
    public static boolean isRecord(PsiClass clazz) {
        if (clazz == null) {
            return false;
        }
        var superClass = clazz.getSuperClass();
        return superClass != null && "java.lang.Record".equals(superClass.getQualifiedName());
    }

}
