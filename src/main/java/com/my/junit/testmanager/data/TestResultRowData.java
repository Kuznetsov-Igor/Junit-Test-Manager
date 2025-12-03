package com.my.junit.testmanager.data;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * DTO одной строки таблицы результатов тестов.
 */
@Value
@Builder(toBuilder = true)
public class TestResultRowData {
    /**
     * Имя класса теста.
     */
    @NotNull
    String testClassName;
    /**
     * Отображаемое имя класса теста (возможно сокращённое).
     */
    @NotNull
    String displayTestClassName;
    /**
     * Полное квалифицированное имя класса теста (с пакетом).
     */
    String classQualifiedName;
    /**
     * Имя метода теста.
     */
    @NotNull
    String methodName;
    /**
     * Текущий результат выполнения теста (например, "Успех", "Ошибка" и т.д.).
     */
    @NotNull
    String currentResult;
    /**
     * Предыдущий результат выполнения теста (например, "Успех", "Ошибка" и т.д.).
     */
    String previousResult;
    /**
     * Длительность выполнения теста в миллисекундах.
     */
    long durationMillis;
    /**
     * Форматированная длительность выполнения теста (например, "1.23 сек").
     */
    @NotNull
    String durationFormatted;
    /**
     * Флаг, указывающий, является ли эта строка сводной (summary).
     */
    boolean summaryRow;
    /**
     * Лог вывода теста.
     */
    String logOutput;
    /**
     * Предыдущий лог вывода теста.
     */
    String previousLogOutput;
    /**
     * URL расположения теста (например, для перехода к коду теста).
     */
    String locationUrl;

    public TestResultRowData withPreviousResult(String previous, String previousLog) {
        return this.toBuilder()
                .previousResult(previous)
                .previousLogOutput(previousLog)
                .build();
    }

    public String getNormalizedMethodName() {
        var name = methodName;
        int idx = name.indexOf('(');
        if (idx == -1) {
            idx = name.indexOf('[');
        }
        if (idx > 0) {
            name = name.substring(0, idx);
        }
        return name.trim();
    }
}

