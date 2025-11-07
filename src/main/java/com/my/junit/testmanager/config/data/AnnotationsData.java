package com.my.junit.testmanager.config.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Модель для хранения данных об аннотациях.
 */
@Data
@NoArgsConstructor
public class AnnotationsData {
    /**
     * Текст аннотации (например, "@MyAnnotation
     */
    private String annotationText;
    /**
     * Тип цели аннотации (например, "METHOD", "FIELD" и т.д.)
     */
    private String targetType;
    /**
     * Импорт необходимый для использования этой аннотации (например, "import com.example.MyAnnotation;")
     */
    private String imports;

    public AnnotationsData(
            @NotNull String annotationText,
            @NotNull String targetType,
            @NotNull String imports
    ) {
        this.annotationText = annotationText;
        this.targetType = targetType;
        this.imports = imports;
    }

    @NotNull
    public static AnnotationsData of(
            @NotNull String annotationText,
            @NotNull String targetType,
            @NotNull String imports
    ) {
        return new AnnotationsData(
                annotationText,
                targetType,
                imports
        );
    }
}
