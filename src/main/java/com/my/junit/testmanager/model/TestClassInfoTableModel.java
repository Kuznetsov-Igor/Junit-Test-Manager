package com.my.junit.testmanager.model;

import com.my.junit.testmanager.data.TestClassInfoData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения информации о тестовых классах.
 */
public class TestClassInfoTableModel extends AbstractTableModel {
    @Getter
    private final List<TestClassInfoData> items;

    private final String[] columnNames = {
            message("table.column.test.class.name"),
            message("table.column.path"),
            message("table.column.group"),
    };

    public TestClassInfoTableModel(@NotNull List<TestClassInfoData> items) {
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
            case 0 -> item.getName();
            case 1 -> item.getPath();
            case 2 -> item.getGroup();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }
}

