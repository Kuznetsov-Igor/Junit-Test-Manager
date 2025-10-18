package com.my.junit.testmanager.config.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Модель для одной группы настроек.
 * Содержит: наименование, регулярное выражение для фильтрации тестов,
 * VM-аргументы, цвет для отображения в UI, список профилей.
 */
@Data
public class GroupConfigData {
    public static final String COLOR_DEFAULT_HEX = "#808080";

    /**
     * Название группы (отображается в UI)
     */
    private String name;
    /**
     * Регулярное выражение для фильтрации тестов
     */
    private String regex;
    /**
     * VM-аргументы для запуска тестов
     */
    private String vmArgs;
    /**
     * Цвет для отображения группы в UI (может быть null, тогда используется цвет по умолчанию)
     */
    private String hexColor;
    /**
     * Список профилей, связанных с этой группой (может быть пустым, но не null)
     */
    private List<ProfileConfigData> profiles;
    /**
     * Дефолтная группа настроек
     */
    public static final GroupConfigData DEFAULT = new GroupConfigData();

    /**
     * Создает группу настроек с заданными параметрами.
     *
     * @param name   - название группы (не null)
     * @param regex  - регулярное выражение (может быть null)
     * @param vmArgs - VM-аргументы (может быть null)
     * @param color  - цвет (может быть null)
     * @return Группа настроек с заданными параметрами.
     */
    public static GroupConfigData of(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable Color color,
            @NotNull List<ProfileConfigData> profiles
    ) {
        return new GroupConfigData(
                name,
                regex,
                vmArgs,
                color,
                profiles
        );
    }

    public GroupConfigData() {
        this(
                "Default",
                null,
                null,
                COLOR_DEFAULT_HEX,
                new ArrayList<>(List.of(ProfileConfigData.DEFAULT))
        );
    }

    public GroupConfigData(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable Color color,
            @Nullable List<ProfileConfigData> profiles
    ) {
        this.name = requireNonNull(name, "Group name cannot be null");
        this.regex = regex;
        this.vmArgs = vmArgs;
        this.hexColor = hexColor(color);
        this.profiles = profiles != null ? new ArrayList<>(profiles) : new ArrayList<>();
    }

    public GroupConfigData(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable String hexColor,
            @Nullable List<ProfileConfigData> profiles
    ) {
        this.name = requireNonNull(name, "Group name cannot be null");
        this.regex = regex;
        this.vmArgs = vmArgs;
        this.hexColor = hexColor;
        this.profiles = profiles != null ? new ArrayList<>(profiles) : new ArrayList<>();
    }

    /**
     * Добавляет профиль в список профилей группы.
     *
     * @param profile - профиль для добавления (не null)
     */
    public void addProfile(@NotNull ProfileConfigData profile) {
        this.profiles.add(profile);
    }

    /**
     * Удаляет профиль из списка профилей группы.
     *
     * @param profile - профиль для удаления (не null)
     */
    public void removeProfile(@NotNull ProfileConfigData profile) {
        this.profiles.remove(profile);
    }

    /**
     * Преобразует цвет в шестнадцатеричное представление.
     *
     * @param color - цвет для преобразования (может быть null)
     * @return Шестнадцатеричное представление цвета в формате "#RRGGBB". Если цвет равен null, возвращается значение
     * по умолчанию "#808080".
     */
    public String hexColor(@Nullable Color color) {
        if (color == null) {
            return COLOR_DEFAULT_HEX;
        }
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    /**
     * Преобразует шестнадцатеричное представление цвета в объект Color.
     * @return Объект Color, соответствующий hexColor.
     */
    public Color getColor() {
        return Color.decode(hexColor);
    }
}
