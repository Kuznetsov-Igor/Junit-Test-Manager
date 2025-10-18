package com.my.junit.testmanager.config.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Модель для одного профиля настроек.
 * Содержит: наименование.
 */
@Data
public class ProfileConfigData {
    /**
     * Название профиля (отображается в UI)
     */
    private String name;

    /**
     * Дефолтный профиль настроек
     */
    public static final ProfileConfigData DEFAULT = new ProfileConfigData();

    public static ProfileConfigData of(
            @NotNull String name
    ) {
        return new ProfileConfigData(
                name
        );
    }

    public ProfileConfigData() {
        this("Default");
    }

    public ProfileConfigData(
            @NotNull String name
    ) {
        this.name = requireNonNull(name, "name cannot be null");
    }

    @Override
    public String toString() {
        return name;
    }
}

