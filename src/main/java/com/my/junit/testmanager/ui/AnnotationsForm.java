package com.my.junit.testmanager.ui;

import com.intellij.openapi.ui.ValidationInfo;
import com.my.junit.testmanager.config.data.AnnotationsData;
import org.jetbrains.annotations.Nullable;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Форма для настройки аннотаций.
 */
public class AnnotationsForm extends AbstractLabelForm<AnnotationsData> {

    public AnnotationsForm() {
        super(
                message("settings.annotations.label.text"),
                message("settings.annotations.label.target"),
                message("settings.annotations.label.import")
        );
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (textFields.size() < 2) {
            return null;
        }

        final var annotationText = textFields.get(0).getText().trim();
        if (annotationText.isEmpty()) {
            return new ValidationInfo(message("validation.error.annotation.text.empty"), textFields.get(0));
        }

        final var targetType = textFields.get(1).getText().trim();
        if (targetType.isEmpty()) {
            return new ValidationInfo(message("validation.error.annotation.target.empty"), textFields.get(1));
        }

        // Проверка на валидные значения targetType
        if (!targetType.equals("CLASS") && !targetType.equals("METHOD")) {
            return new ValidationInfo(
                    message("validation.error.annotation.target.invalid", targetType),
                    textFields.get(1)
            );
        }

        return null;
    }

    @Override
    protected AnnotationsData getObject() {
        return AnnotationsData.of(
                textFields.get(0).getText(),
                textFields.get(1).getText(),
                textFields.get(2).getText()
        );
    }
}
