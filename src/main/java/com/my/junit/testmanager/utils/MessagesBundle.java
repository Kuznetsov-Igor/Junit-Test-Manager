package com.my.junit.testmanager.utils;

import com.my.junit.testmanager.config.TestManagerSettings;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Утилитарный класс для работы с ресурсными бандлами сообщений.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MessagesBundle {
    private static final String BUNDLE_BASE_NAME = "message";
    private static final ConcurrentHashMap<Locale, ResourceBundle> bundleCache = new ConcurrentHashMap<>();

    /**
     * Получение сообщения по ключу с текущей локалью и форматированием.
     *
     * @param key    ключ сообщения
     * @param params параметры для форматирования
     * @return сообщение или "!key!" + key + "!", если ключ не найден
     */
    public static String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE_BASE_NAME) String key,
            @NotNull Object... params
    ) {
        final var settings = TestManagerSettings.getInstance();
        return message(settings.getLanguage(), key, params);
    }

    /**
     * Получение сообщения по ключу с указанной локалью и форматированием.
     * Позволяет переопределить локаль для конкретного вызова.
     *
     * @param locale локаль (например, Locale.forLanguageTag("ru"))
     * @param key    ключ сообщения
     * @param params параметры для форматирования
     * @return сообщение или "!key!" + key + "!", если ключ не найден
     */
    private static String message(
            @NotNull Locale locale,
            @NotNull @PropertyKey(resourceBundle = BUNDLE_BASE_NAME) String key,
            @NotNull Object... params
    ) {
        try {
            final var bundle = getBundle(locale);
            final var template = bundle.getString(key);
            return MessageFormat.format(template, params);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * Получение ресурсного бандла для указанной локали с кэшированием.
     *
     * @param locale локаль
     * @return ресурсный бандл
     */
    private static ResourceBundle getBundle(@NotNull Locale locale) {
        return bundleCache.computeIfAbsent(locale, loc -> {
            try {
                return ResourceBundle.getBundle(
                        BUNDLE_BASE_NAME,
                        loc,
                        MessagesBundle.class.getClassLoader()
                );
            } catch (Exception e) {
                return ResourceBundle.getBundle(
                        BUNDLE_BASE_NAME,
                        Locale.ROOT,
                        MessagesBundle.class.getClassLoader()
                );
            }
        });
    }
}






