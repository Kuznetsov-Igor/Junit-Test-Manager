package com.my.junit.testmanager.config.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Модель для хранения данных о фабричном методе.
 */
@Data
@NoArgsConstructor
public class FactoryMethodData {
    /**
     * Полное имя фабричного класса, например "com.example.MyFactory".
     */
    private String factoryClass;
    /**
     * Название метода фабрики, например "createObject".
     */
    private String methodName;

    /**
     * Импорт необходимый для использования этого фабричного метода, например "import com.example.MyFactory;"
     */
    private String imports;

    public FactoryMethodData(
            @NotNull String factoryClass,
            @NotNull String methodName,
            @NotNull String imports
    ) {
        this.factoryClass = factoryClass;
        this.methodName = methodName;
        this.imports = imports;
        RandomStringUtils.randomAlphabetic(5);
    }

    public static FactoryMethodData of(
            @NotNull String factoryClass,
            @NotNull String methodName,
            @NotNull String imports
    ) {
       return new FactoryMethodData(
                factoryClass,
                methodName,
                imports
       );
    }
}
