package com.my.junit.testmanager.data;

import com.intellij.psi.PsiClass;
import com.my.junit.testmanager.config.data.GroupData;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Данные о тестовом классе.
 */
@Data
public class TestClassInfoData {
    /**
     * Имя тестового класса.
     */
    private final String name;
    /**
     * Путь к тестовому классу.
     */
    private final String path;
    /**
     * PsiClass тестового класса.
     */
    private final PsiClass psiClass;
    /**
     * Группа, к которой относится тестовый класс.
     */
    private GroupData group = GroupData.DEFAULT;

    public static TestClassInfoData of(
            @NotNull String name,
            @NotNull PsiClass psiClass
    ) {
        return new TestClassInfoData(
                name,
                psiClass
        );
    }

    private TestClassInfoData(
            @NotNull String name,
            @NotNull PsiClass psiClass
    ) {
        final var fqcn = psiClass.getQualifiedName();
        this.name = name;
        this.path = fqcn != null
                ? fqcn
                : psiClass.getContainingFile()
                        .getVirtualFile()
                        .getPath();
        this.psiClass = psiClass;
    }
}
