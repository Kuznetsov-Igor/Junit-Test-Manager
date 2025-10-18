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
        final var allTestClasses = PsiUtils.findClassesByName(project, expectedTestName)
                .stream()
                .filter(testClass -> PsiUtils.isInTestSourceRoot(
                        testClass,
                        project
                ))
                .toList();

        for (var testClass : allTestClasses) {
            if (PsiUtils.hasImportOnClass(testClass, psiClass)) {
                log.logInfo(
                        "Found test class: " + testClass.getQualifiedName()
                                + " for class: " + psiClass.getQualifiedName()
                );
                final var testFqcn = testClass.getQualifiedName();
                final var classFqcn = psiClass.getQualifiedName();

                if (testFqcn != null && classFqcn != null && !PsiUtils.isPackageMatching(classFqcn, testFqcn)) {
                    log.logInfo("Found test class with mismatched package: " + testFqcn + " for class: " + classFqcn);

                    final var newTestFqcn = PsiUtils.calculateNewTestFqcnForMismatch(classFqcn, expectedTestName);

                    return TestClassRelocationData.of(
                            expectedTestName,
                            PsiUtils.extractPackageFromFqcn(testFqcn),
                            newTestFqcn,
                            testClass
                    );
                }
            }
        }
        return null;
    }
}
