package com.my.junit.testmanager.ui;


import com.my.junit.testmanager.config.data.AnnotationsData;

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

    @Override
    protected AnnotationsData getObject() {
        return AnnotationsData.of(
                textFields.get(0).getText(),
                textFields.get(1).getText(),
                textFields.get(2).getText()
        );
    }
}
