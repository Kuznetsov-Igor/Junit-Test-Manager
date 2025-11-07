package com.my.junit.testmanager.model;

import com.my.junit.testmanager.config.data.FactoryMethodData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения фабричных методов.
 */
public class FactoryMethodTableModel extends AbstractBaseTableModel<FactoryMethodData> {
    private final String[] columnNames = {
            message("settings.factory.method.table.column.class"),
            message("settings.factory.method.table.column.name"),
            message("settings.factory.method.table.column.imports")
    };

    public FactoryMethodTableModel(@NotNull List<FactoryMethodData> items) {
        super(items);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            return null;
        }
        final var item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getFactoryClass();
            case 1 -> item.getMethodName();
            case 2 -> item.getImports();
            default -> null;
        };
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }
}

