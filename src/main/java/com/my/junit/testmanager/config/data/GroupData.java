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
public class GroupData {
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
    private List<ProfileData> profiles;
    /**
     * Дефолтная группа настроек
     */
    public static final GroupData DEFAULT = new GroupData();

    /**
     * Создает группу настроек с заданными параметрами.
     *
     * @param name   - название группы (не null)
     * @param regex  - регулярное выражение (может быть null)
     * @param vmArgs - VM-аргументы (может быть null)
     * @param color  - цвет (может быть null)
     * @return Группа настроек с заданными параметрами.
     */
    public static GroupData of(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable Color color,
            @NotNull List<ProfileData> profiles
    ) {
        return new GroupData(
                name,
                regex,
                vmArgs,
                color,
                profiles
        );
    }

    public GroupData() {
        this(
                "Default",
                null,
                null,
                COLOR_DEFAULT_HEX,
                new ArrayList<>(List.of(ProfileData.DEFAULT))
        );
    }

    public GroupData(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable Color color,
            @Nullable List<ProfileData> profiles
    ) {
        this.name = requireNonNull(name, "Group name cannot be null");
        this.regex = regex;
        this.vmArgs = vmArgs;
        this.hexColor = hexColor(color);
        this.profiles = profiles != null ? new ArrayList<>(profiles) : new ArrayList<>();
    }

    public GroupData(
            @NotNull String name,
            @Nullable String regex,
            @Nullable String vmArgs,
            @Nullable String hexColor,
            @Nullable List<ProfileData> profiles
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
    public void addProfile(@NotNull ProfileData profile) {
        this.profiles.add(profile);
    }

    /**
     * Удаляет профиль из списка профилей группы.
     *
     * @param profile - профиль для удаления (не null)
     */
    public void removeProfile(@NotNull ProfileData profile) {
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
