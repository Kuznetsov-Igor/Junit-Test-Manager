package com.my.junit.testmanager.ui;


import com.my.junit.testmanager.config.data.FactoryMethodData;

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

    @Override
    protected FactoryMethodData getObject() {
        return FactoryMethodData.of(
                textFields.get(0).getText(),
                textFields.get(1).getText(),
                textFields.get(2).getText()
        );
    }
}
