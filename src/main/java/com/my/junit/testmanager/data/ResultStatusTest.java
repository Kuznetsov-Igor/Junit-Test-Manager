package com.my.junit.testmanager.data;

public enum ResultStatusTest {
    /**
     * Тест пройден успешно.
     */
    PASSED,
    /**
     * Тест завершился с предупреждением.
     */
    WARNING,
    /**
     * Тест завершился с ошибкой.
     */
    FAILED,
    /**
     * Тест был проигнорирован.
     */
    IGNORED
}
