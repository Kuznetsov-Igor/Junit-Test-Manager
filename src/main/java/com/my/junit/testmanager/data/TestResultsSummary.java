package com.my.junit.testmanager.data;

import lombok.Builder;
import lombok.Value;

/**
 * Агрегированная информация о запуске тестов.
 */
@Value
@Builder
public class TestResultsSummary {
    /**
     * Общее время выполнения всех тестов в формате строки (например, "12.34 сек").
     */
    String totalDuration;
    /**
     * Количество успешно пройденных тестов.
     */
    int passedCount;
    /**
     * Количество проваленных тестов.
     */
    int warningCount;
    /**
     * Количество тестов, завершившихся с ошибкой.
     */
    int failedCount;
}

