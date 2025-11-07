package com.my.junit.testmanager.model;

import com.my.junit.testmanager.data.TestClassRelocationData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения перемещений тестовых классов.
 */
public class TestClassRelocationTableModel extends AbstractBaseTableModel<TestClassRelocationData> {
    private final String[] columnNames = {
            message("table.relocation.column.selected"),
            message("table.column.name"),
            message("table.relocation.column.old.path"),
            message("table.relocation.column.new.path"),
    };

    public TestClassRelocationTableModel(@NotNull List<TestClassRelocationData> items) {
        super(items);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            return null;
        }
        final var item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.isSelected();
            case 1 -> item.getName();
            case 2 -> item.getOldPath();
            case 3 -> item.getNewPath();
            default -> null;
        };
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size() || columnIndex != 0 || !(aValue instanceof Boolean)) {
            return;
        }
        items.get(rowIndex).setSelected((Boolean) aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }
}

