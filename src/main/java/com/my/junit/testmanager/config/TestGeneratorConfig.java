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
            // Дефолтные фабричные методы
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "String",
                            "randomAlphabetic(10)",
                            "import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "int",
                            "new Random().nextInt()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "Integer",
                            "new Random().nextInt()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "long",
                            "new Random().nextLong()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "Long",
                            "new Random().nextLong()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "boolean",
                            "new Random().nextBoolean()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "Boolean",
                            "new Random().nextBoolean()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "double",
                            "new Random().nextDouble()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "Double",
                            "new Random().nextDouble()",
                            "import java.util.Random;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.util.UUID",
                            "UUID.randomUUID()",
                            "import java.util.UUID;"
                    )
            );
            // Дополнительные базовые типы
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.time.LocalDate",
                            "LocalDate.now()",
                            "import java.time.LocalDate;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.time.Instant",
                            "Instant.now()",
                            "import java.time.Instant;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.time.LocalDateTime",
                            "LocalDateTime.now()",
                            "import java.time.LocalDateTime;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.time.LocalTime",
                            "LocalTime.now()",
                            "import java.time.LocalTime;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.math.BigDecimal",
                            "new BigDecimal(\"100.00\")",
                            "import java.math.BigDecimal;"
                    )
            );
            factoryMethodDataList.add(
                    FactoryMethodData.of(
                            "java.math.BigInteger",
                            "new BigInteger(\"100\")",
                            "import java.math.BigInteger;"
                    )
            );
        }
        return factoryMethodDataList;
    }


}
