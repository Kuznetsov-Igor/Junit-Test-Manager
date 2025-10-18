package com.my.junit.testmanager.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.my.junit.testmanager.config.TestManagerSettings;
import com.my.junit.testmanager.data.SearchType;
import com.my.junit.testmanager.data.TestClassInfoData;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Сервис для поиска тестовых классов в проекте.
 */
public class TestClassesFinder {
    private final LoggerUtils log = LoggerUtils.getLogger(TestClassesFinder.class);

    /**
     * Проект IntelliJ IDEA, в котором выполняется поиск.
     */
    private final Project project;

    public TestClassesFinder(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Собирает тестовые классы в проекте в зависимости от типа поиска.
     *
     * @param searchType тип поиска (все классы, измененные классы или классы в директории)
     * @param directory  директория для поиска (только для типа DIRECTORY)
     * @return список данных о тестовых классах
     */
    public List<TestClassInfoData> collect(
            @NotNull SearchType searchType,
            @Nullable VirtualFile directory
    ) {
        return switch (searchType) {
            case ALL -> findAllTestClasses();
            case CHANGES -> findTestClassesInChanges();
            case DIRECTORY -> findTestClassesInDirectory(requireNonNull(directory));
        };
    }

    /**
     * Ищем все тестовые классы в проекте.
     *
     * @return список данных о тестовых классах
     */
    private List<TestClassInfoData> findAllTestClasses() {
        log.logInfo("Collecting all test classes in project...");
        final var psiClasses = PsiUtils.getAllPsiClassesFromAllModules(
                project,
                JavaSourceRootType.TEST_SOURCE
        );
        return psiClasses
                .stream()
                .map(this::createTestClassInfoData)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Ищем тестовые классы в измененных файлах.
     *
     * @return список данных о тестовых классах
     */
    @NotNull
    private List<TestClassInfoData> findTestClassesInChanges() {
        log.logInfo("Collecting test classes in changed files...");
        final var psiClasses = PsiUtils.getChangedPsiClassesFromAllModules(
                project,
                JavaSourceRootType.TEST_SOURCE
        );
        return psiClasses
                .stream()
                .map(this::createTestClassInfoData)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Ищем тестовые классы в указанной директории.
     *
     * @param directory директория для поиска тестовых классов
     * @return список данных о тестовых классах
     */
    @NotNull
    private List<TestClassInfoData> findTestClassesInDirectory(@NotNull VirtualFile directory) {
        log.logInfo("Collecting test classes in directory: " + directory.getPath());
        final var psiClasses = PsiUtils.getAllPsiClassesFromDirectory(
                project,
                directory
        );
        return psiClasses
                .stream()
                .map(this::createTestClassInfoData)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Создает объект TestClassInfoData из PsiClass, если это тестовый класс.
     *
     * @param psiClass класс для анализа
     * @return объект TestClassInfoData или null, если класс не является тестовым
     */
    @Nullable
    private TestClassInfoData createTestClassInfoData(
            @NotNull PsiClass psiClass
    ) {
        log.logInfo("Filtering PsiClass: " + psiClass.getName());
        if (!PsiUtils.isTestClass(psiClass)) {
            log.logInfo("PsiClass is not a test class: " + psiClass.getName());
            return null;
        }
        final var simpleName = psiClass.getName();
        if (simpleName == null) {
            log.logWarn("PsiClass with null name: " + psiClass.getText());
            return null;
        }
        log.logInfo("Found test class: " + simpleName);
        final var testClassInfo = TestClassInfoData.of(
                simpleName,
                psiClass
        );
        determineGroup(testClassInfo);
        return testClassInfo;
    }


    /**
     * Определяет группу для тестового класса на основе его пути и настроек.
     *
     * @param testClassInfoData данные о тестовом классе
     */
    private void determineGroup(@NotNull TestClassInfoData testClassInfoData) {
        log.logInfo("Determining group for test class: " + testClassInfoData.getName());

        final var settings = TestManagerSettings.getInstance();

        final var groups = settings.getGroups();
        final var assigned = groups.stream()
                .filter(g -> g.getProfiles().contains(settings.getActiveProfile()))
                .filter(g -> g.getRegex() != null &&
                        Pattern.compile(g.getRegex()).matcher(testClassInfoData.getPath()).find())
                .findFirst()
                .orElse(null);

        if (assigned != null) {
            testClassInfoData.setGroup(assigned);
            log.logInfo("Assigned group: " + assigned.getName() + " to test class: " + testClassInfoData.getName());
        } else {
            log.logInfo(
                    "No matching group found. Assigned default group to test class: " + testClassInfoData.getName()
            );
        }
    }
}




