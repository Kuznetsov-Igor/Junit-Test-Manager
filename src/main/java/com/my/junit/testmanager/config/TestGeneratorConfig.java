package com.my.junit.testmanager.config;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.my.junit.testmanager.config.data.AnnotationsData;
import com.my.junit.testmanager.config.data.FactoryMethodData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Настройки генератора тестов.
 */
@State(
        name = "JunitTestManager_TestGeneratorSettings",
        storages = @Storage(value = "JunitTestManager_TestGeneratorSettings.xml")
)
@Data
@EqualsAndHashCode(callSuper = true)
public class TestGeneratorConfig extends AbstractPersistentStateComponent<TestGeneratorConfig> {

    /**
     * Имя класса генератора.
     */
    private String generatorClassName = "Generator";
    /**
     * Имя метода генерации.
     */
    private String generatorMethodName = "generate";
    /**
     * Список данных об аннотациях.
     */
    private List<AnnotationsData> annotationsDataList;
    /**
     * Список данных о фабричных методах.
     */
    private List<FactoryMethodData> factoryMethodDataList;

    @NotNull
    public static TestGeneratorConfig getInstance(@NotNull Project project) {
        final var instance = project.getService(TestGeneratorConfig.class);
        if (instance == null) {
            return new TestGeneratorConfig();
        }
        return instance;
    }

    public boolean isStateEquals(@NotNull TestGeneratorConfig other) {
        return this.generatorClassName.equals(other.generatorClassName)
                && this.generatorMethodName.equals(other.generatorMethodName)
                && isListEquals(this.annotationsDataList, other.annotationsDataList)
                && isListEquals(this.factoryMethodDataList, other.factoryMethodDataList);
    }

    @Override
    @Nullable
    public TestGeneratorConfig getState() {
        return this;
    }

    public List<AnnotationsData> getAnnotationsDataList() {
        if (annotationsDataList == null) {
            annotationsDataList = new ArrayList<>();
            // Дефолтные аннотации
            annotationsDataList.add(
                    AnnotationsData.of("@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)", "CLASS",
                            "import lombok.NoArgsConstructor;"));
            annotationsDataList.add(
                    AnnotationsData.of("@NonNull", "METHOD", "import org.springframework.lang.NonNull;"));
        }
        return annotationsDataList;
    }

    public List<FactoryMethodData> getFactoryMethodDataList() {
        if (factoryMethodDataList == null) {
            factoryMethodDataList = new ArrayList<>();
            initializeDefaultFactoryMethods();
        }
        return factoryMethodDataList;
    }

    /**
     * Инициализирует дефолтные фабричные методы для различных типов.
     */
    private void initializeDefaultFactoryMethods() {
        addStringFactoryMethod();
        addPrimitiveNumericFactoryMethods();
        addPrimitiveBooleanFactoryMethods();
        addUuidFactoryMethod();
        addTimeFactoryMethods();
        addBigNumberFactoryMethods();
    }

    /**
     * Добавляет фабричный метод для String.
     */
    private void addStringFactoryMethod() {
        factoryMethodDataList.add(FactoryMethodData.of(
                "String",
                "randomAlphabetic(10)",
                "import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;"
        ));
    }

    /**
     * Добавляет фабричные методы для примитивных числовых типов и их обёрток.
     */
    private void addPrimitiveNumericFactoryMethods() {
        final var randomImport = "import java.util.Random;";
        factoryMethodDataList.add(FactoryMethodData.of("int", "new Random().nextInt()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("Integer", "new Random().nextInt()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("long", "new Random().nextLong()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("Long", "new Random().nextLong()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("double", "new Random().nextDouble()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("Double", "new Random().nextDouble()", randomImport));
    }

    /**
     * Добавляет фабричные методы для примитивного boolean и его обёртки.
     */
    private void addPrimitiveBooleanFactoryMethods() {
        final var randomImport = "import java.util.Random;";
        factoryMethodDataList.add(FactoryMethodData.of("boolean", "new Random().nextBoolean()", randomImport));
        factoryMethodDataList.add(FactoryMethodData.of("Boolean", "new Random().nextBoolean()", randomImport));
    }

    /**
     * Добавляет фабричный метод для UUID.
     */
    private void addUuidFactoryMethod() {
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.util.UUID",
                "UUID.randomUUID()",
                "import java.util.UUID;"
        ));
    }

    /**
     * Добавляет фабричные методы для типов времени из java.time.
     */
    private void addTimeFactoryMethods() {
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.time.LocalDate",
                "LocalDate.now()",
                "import java.time.LocalDate;"
        ));
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.time.Instant",
                "Instant.now()",
                "import java.time.Instant;"
        ));
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.time.LocalDateTime",
                "LocalDateTime.now()",
                "import java.time.LocalDateTime;"
        ));
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.time.LocalTime",
                "LocalTime.now()",
                "import java.time.LocalTime;"
        ));
    }

    /**
     * Добавляет фабричные методы для больших чисел (BigDecimal, BigInteger).
     */
    private void addBigNumberFactoryMethods() {
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.math.BigDecimal",
                "new BigDecimal(\"100.00\")",
                "import java.math.BigDecimal;"
        ));
        factoryMethodDataList.add(FactoryMethodData.of(
                "java.math.BigInteger",
                "new BigInteger(\"100\")",
                "import java.math.BigInteger;"
        ));
    }

    /**
     * Находит фабричный метод для заданного типа.
     * Ищет по полному имени типа (canonical name) в списке фабричных методов.
     *
     * @param canonicalTypeName Полное имя типа (например, "int", "String", "java.util.UUID").
     * @return FactoryMethodData если найден, иначе null.
     */
    @Nullable
    public FactoryMethodData findFactoryMethodForType(@NotNull String canonicalTypeName) {
        return getFactoryMethodDataList().stream()
                .filter(factory -> factory.getFactoryClass().equals(canonicalTypeName))
                .findFirst()
                .orElse(null);
    }

}
