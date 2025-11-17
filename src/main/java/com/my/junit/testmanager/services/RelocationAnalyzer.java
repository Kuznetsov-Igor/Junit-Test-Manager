package com.my.junit.testmanager.services;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.my.junit.testmanager.data.SearchType;
import com.my.junit.testmanager.data.TestClassRelocationData;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для анализа релокации тестовых классов.
 */
public class RelocationAnalyzer {
    private final LoggerUtils log = LoggerUtils.getLogger(RelocationAnalyzer.class);

    /**
     * Проект IntelliJ IDEA, в котором выполняется анализ.
     */
    private final Project project;

    public RelocationAnalyzer(
            @NotNull Project project
    ) {
        this.project = project;
    }

    /**
     * Анализирует тестовые классы для релокации в зависимости от типа поиска.
     *
     * @param searchType тип поиска (все классы или только измененные)
     * @return список данных о релокации тестовых классов
     */
    public List<TestClassRelocationData> analyze(
            @NotNull SearchType searchType
    ) {
        return switch (searchType) {
            case ALL -> getAllClasses();
            case CHANGES -> getChangedClasses();
            default -> {
                log.logWarn("Unknown search type: " + searchType);
                yield new ArrayList<>();
            }
        };
    }

    /**
     * Получает все классы в проекте и анализирует их для релокации тестовых классов.
     *
     * @return список данных о релокации тестовых классов
     */
    @NotNull
    private List<TestClassRelocationData> getAllClasses() {
        log.logInfo("Analyzing all classes for test relocation...");
        final var psiClasses = PsiUtils.getAllPsiClassesFromAllModules(
                project,
                JavaSourceRootType.SOURCE
        );
        return psiClasses.stream()
                .map(this::createTestClassRelocationItem)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Получает измененные классы в проекте и анализирует их для релокации тестовых классов.
     *
     * @return список данных о релокации тестовых классов
     */
    @NotNull
    private List<TestClassRelocationData> getChangedClasses() {
        log.logInfo("Analyzing changed classes for test relocation...");
        final var psiClasses = PsiUtils.getChangedPsiClassesFromAllModules(
                project,
                JavaSourceRootType.SOURCE
        );
        return psiClasses.stream()
                .map(this::createTestClassRelocationItem)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Создает элемент данных о релокации тестового класса для заданного класса.
     *
     * @param psiClass класс для анализа
     * @return элемент данных о релокации тестового класса или null, если релокация не требуется
     */
    @Nullable
    private TestClassRelocationData createTestClassRelocationItem(@NotNull PsiClass psiClass) {
        final var className = psiClass.getName();
        if (className == null) {
            log.logInfo("Class name is null, skipping.");
            return null;
        }

        final var expectedTestName = className + "Test";
        final var testClasses = findTestClassesForRelocation(expectedTestName);

        return findMismatchedTestClass(testClasses, psiClass, expectedTestName);
    }

    /**
     * Находит тестовые классы с заданным именем в тестовых исходниках.
     *
     * @param testName имя тестового класса
     * @return список тестовых классов
     */
    @NotNull
    private List<PsiClass> findTestClassesForRelocation(@NotNull String testName) {
        return PsiUtils.findClassesByName(project, testName)
                .stream()
                .filter(testClass -> PsiUtils.isInTestSourceRoot(testClass, project))
                .toList();
    }

    /**
     * Находит тестовый класс с несоответствующим пакетом для релокации.
     *
     * @param testClasses список тестовых классов для проверки
     * @param sourceClass исходный класс
     * @param expectedTestName ожидаемое имя тестового класса
     * @return данные о релокации или null, если несоответствия не найдены
     */
    @Nullable
    private TestClassRelocationData findMismatchedTestClass(
            @NotNull List<PsiClass> testClasses,
            @NotNull PsiClass sourceClass,
            @NotNull String expectedTestName
    ) {
        final var classFqcn = sourceClass.getQualifiedName();
        if (classFqcn == null) {
            return null;
        }

        for (var testClass : testClasses) {
            if (!PsiUtils.hasImportOnClass(testClass, sourceClass)) {
                continue;
            }

            final var testFqcn = testClass.getQualifiedName();
            if (testFqcn == null) {
                continue;
            }

            log.logInfo("Found test class: " + testFqcn + " for class: " + classFqcn);

            if (!isPackageMatching(classFqcn, testFqcn)) {
                log.logInfo("Found test class with mismatched package: " + testFqcn + " for class: " + classFqcn);
                return createRelocationData(testFqcn, classFqcn, expectedTestName, testClass);
            }
        }
        return null;
    }

    /**
     * Создает данные о релокации для тестового класса.
     *
     * @param testFqcn полное имя тестового класса
     * @param classFqcn полное имя исходного класса
     * @param expectedTestName ожидаемое имя тестового класса
     * @param testClass тестовый класс
     * @return данные о релокации
     */
    @NotNull
    private TestClassRelocationData createRelocationData(
            @NotNull String testFqcn,
            @NotNull String classFqcn,
            @NotNull String expectedTestName,
            @NotNull PsiClass testClass
    ) {
        final var newTestFqcn = calculateNewTestFqcnForMismatch(classFqcn, expectedTestName);
        return TestClassRelocationData.of(
                expectedTestName,
                PsiUtils.extractPackageFromFqcn(testFqcn),
                newTestFqcn,
                testClass
        );
    }

    /**
     * Проверяет, совпадают ли пакеты у двух FQCN.
     *
     * @param classFqcn FQCN класса.
     * @param testFqcn  FQCN тестового класса.
     * @return true, если пакеты совпадают, иначе false.
     */
    private boolean isPackageMatching(@NotNull String classFqcn, @NotNull String testFqcn) {
        final var classPackage = PsiUtils.extractPackageFromFqcn(classFqcn);
        final var testPackage = PsiUtils.extractPackageFromFqcn(testFqcn);
        log.logInfo("Comparing packages - Class: " + classPackage + ", Test: " + testPackage);
        return classPackage.equals(testPackage);
    }

    /**
     * Вычисляет новый FQCN для теста при несовпадении пакетов.
     * Исправляет пакет теста, чтобы он соответствовал пакету исходного класса.
     *
     * @param classFqcn FQCN исходного класса.
     * @param testName  имя тестового класса.
     * @return новый FQCN для теста (package.className).
     */
    @NotNull
    private String calculateNewTestFqcnForMismatch(@NotNull String classFqcn, @NotNull String testName) {
        log.logInfo("Calculating new FQCN for test: " + testName + " based on class FQCN: " + classFqcn);
        final var classPackage = PsiUtils.extractPackageFromFqcn(classFqcn);
        return classPackage.isEmpty() ? testName : classPackage + "." + testName;
    }
}
