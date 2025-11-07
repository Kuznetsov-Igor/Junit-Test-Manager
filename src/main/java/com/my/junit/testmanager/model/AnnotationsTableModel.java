package com.my.junit.testmanager.model;

import com.my.junit.testmanager.config.data.AnnotationsData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения аннотаций.
 */
public class AnnotationsTableModel extends AbstractBaseTableModel<AnnotationsData> {

    private final String[] columnNames = {
            message("settings.annotations.table.column.text"),
            message("settings.annotations.table.column.target"),
            message("settings.annotations.table.column.imports"),
    };

    public AnnotationsTableModel(@NotNull List<AnnotationsData> items) {
        super(items);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            return null;
        }
        final var item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getAnnotationText();
            case 1 -> item.getImports();
            case 2 -> item.getTargetType();
            default -> null;
        };
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }
}

