package com.my.junit.testmanager.model;

import com.my.junit.testmanager.data.TestClassRelocationData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения перемещений тестовых классов.
 */
public class TestClassRelocationTableModel extends AbstractTableModel {
    @Getter
    private final List<TestClassRelocationData> items;

    private final String[] columnNames = {
            message("table.relocation.column.selected"),
            message("table.column.name"),
            message("table.relocation.column.old.path"),
            message("table.relocation.column.new.path"),
    };

    public TestClassRelocationTableModel(@NotNull List<TestClassRelocationData> items) {
        this.items = items;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size() || columnIndex != 0 || !(aValue instanceof Boolean)) {
            return;
        }
        items.get(rowIndex).setSelected((Boolean) aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }
}

