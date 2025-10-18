package com.my.junit.testmanager.data;

import com.intellij.psi.PsiClass;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Данные о релокации тестового класса.
 */
@Data
public class TestClassRelocationData {
    /**
     * Имя тестового класса.
     */
    private final String name;
    /**
     * Старый путь к тестовому классу.
     */
    private final String oldPath;
    /**
     * Новый путь к тестовому классу.
     */
    private final String newPath;
    /**
     * Флаг, выбран ли тестовый класс для релокации.
     */
    private boolean selected = false;
    /**
     * PsiClass тестового класса.
     */
    private final PsiClass psiClass;

    public static TestClassRelocationData of(
            @NotNull String name,
            @NotNull String oldPath,
            @NotNull String newPath,
            @NotNull PsiClass psiClass
    ) {
        return new TestClassRelocationData(name, oldPath, newPath, psiClass);
    }

    private TestClassRelocationData(
            @NotNull String name,
            @NotNull String oldPath,
            @NotNull String newPath,
            @NotNull PsiClass psiClass
    ) {
        this.name = name;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.psiClass = psiClass;
    }
}

