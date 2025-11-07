package com.my.junit.testmanager.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.my.junit.testmanager.config.TestManagerConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Утилитарный класс для логирования с учётом настроек плагина.
 */
public class LoggerUtils {
    private final Logger logger;

    public static LoggerUtils getLogger(
            @NotNull Class<?> clazz
    ) {
        return new LoggerUtils(clazz);
    }

    private LoggerUtils(
            @NotNull Class<?> clazz
    ) {
        this.logger = Logger.getInstance(clazz);
    }

    /**
     * Логирует информационное сообщение, если логирование включено в настройках плагина.
     *
     * @param message Сообщение для логирования.
     */
    public void logInfo(
            @NotNull String message
    ) {
        if (isLoggingEnabled()) {
            logger.info(message);
        }
    }

    /**
     * Логирует ошибку с сообщением и исключением, если логирование включено в настройках плагина.
     *
     * @param message   Сообщение для логирования.
     * @param throwable Исключение для логирования (может быть null).
     */
    public void logError(
            @NotNull String message,
            @Nullable Throwable throwable
    ) {
        if (isLoggingEnabled()) {
            logger.error(message, throwable);
        }
    }

    /**
     * Логирует ошибку с сообщением, если логирование включено в настройках плагина.
     *
     * @param message Сообщение для логирования.
     */
    public void logError(
            @NotNull String message
    ) {
        if (isLoggingEnabled()) {
            logger.error(message);
        }
    }

    /**
     * Логирует предупреждающее сообщение, если логирование включено в настройках плагина.
     *
     * @param message Сообщение для логирования.
     */
    public void logWarn(
            @NotNull String message
    ) {
        if (isLoggingEnabled()) {
            logger.warn(message);
        }
    }

    /**
     * Проверяет, включено ли логирование в настройках плагина.
     *
     * @return true, если логирование включено; false в противном случае.
     */
    private boolean isLoggingEnabled() {
        return TestManagerConfig.getInstance().isLoggingEnabled();
    }

}
