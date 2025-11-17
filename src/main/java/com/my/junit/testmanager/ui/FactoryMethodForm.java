package com.my.junit.testmanager.ui;

import com.intellij.openapi.ui.ValidationInfo;
import com.my.junit.testmanager.config.data.FactoryMethodData;
import org.jetbrains.annotations.Nullable;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Форма для настройки фабричного метода.
 */
public class FactoryMethodForm extends AbstractLabelForm<FactoryMethodData> {

    public FactoryMethodForm() {
        super(
                message("settings.factory.method.label.class"),
                message("settings.factory.method.label.name"),
                message("settings.factory.method.label.import")
        );
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (textFields.size() < 3) {
            return null;
        }

        final var className = textFields.get(0).getText().trim();
        if (className.isEmpty()) {
            return new ValidationInfo(message("validation.error.factory.method.class.empty"), textFields.get(0));
        }

        final var methodName = textFields.get(1).getText().trim();
        if (methodName.isEmpty()) {
            return new ValidationInfo(message("validation.error.factory.method.name.empty"), textFields.get(1));
        }

        return null;
    }

    @Override
    protected FactoryMethodData getObject() {
        return FactoryMethodData.of(
                textFields.get(0).getText(),
                textFields.get(1).getText(),
                textFields.get(2).getText()
        );
    }
}
