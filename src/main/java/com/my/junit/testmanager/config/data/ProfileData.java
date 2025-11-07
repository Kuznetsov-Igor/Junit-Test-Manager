package com.my.junit.testmanager.config.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Модель для одного профиля настроек.
 * Содержит: наименование.
 */
@Data
public class ProfileData {
    /**
     * Название профиля (отображается в UI)
     */
    private String name;

    /**
     * Дефолтный профиль настроек
     */
    public static final ProfileData DEFAULT = new ProfileData();

    public static ProfileData of(
            @NotNull String name
    ) {
        return new ProfileData(
                name
        );
    }

    public ProfileData() {
        this("Default");
    }

    public ProfileData(
            @NotNull String name
    ) {
        this.name = requireNonNull(name, "name cannot be null");
    }

    @Override
    public String toString() {
        return name;
    }
}

