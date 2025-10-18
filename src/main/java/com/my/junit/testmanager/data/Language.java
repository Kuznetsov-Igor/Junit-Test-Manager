package com.my.junit.testmanager.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

/**
 * Перечисление поддерживаемых языков интерфейса плагина.
 */
public enum Language {
    ENGLISH("English", Locale.ENGLISH),
    RUSSIAN("Russian", Locale.forLanguageTag("ru"));

    public static final Language DEFAULT = ENGLISH;

    @NotNull
    private final String displayName;

    @NotNull
    private final Locale locale;

    Language(@NotNull String displayName, @NotNull Locale locale) {
        this.displayName = displayName;
        this.locale = locale;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Locale getLocale() {
        return locale;
    }

    /**
     * Получает язык по его отображаемому имени.
     * @param displayName - отображаемое имя языка
     * @return Соответствующий язык или язык по умолчанию, если не найдено совпадений.
     */
    public static Language getLocaleFromDisplay(@Nullable String displayName) {
        if (displayName == null) {
            return Language.DEFAULT;
        }
        return Arrays.stream(values())
                .filter(lang -> lang.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(Language.DEFAULT);
    }
}

