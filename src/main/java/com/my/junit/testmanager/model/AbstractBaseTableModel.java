package com.my.junit.testmanager.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовая абстрактная модель таблицы для отображения
 *
 * @param <T> Тип данных, хранящихся в таблице
 */
@Getter
public abstract class AbstractBaseTableModel<T> extends AbstractTableModel {

    protected final List<T> items;

    protected AbstractBaseTableModel(@NotNull List<T> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public abstract Object getValueAt(int rowIndex, int columnIndex);

    public abstract String[] getColumnNames();

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public String getColumnName(int column) {
        return getColumnNames()[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    public void addRow(@NotNull T item) {
        items.add(item);
        fireTableRowsInserted(items.size() - 1, items.size() - 1);
    }

    public void updateRow(int rowIndex, @NotNull T item) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            items.set(rowIndex, item);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public void setItems(@NotNull List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        fireTableDataChanged();
    }

    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            items.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    public T getItemAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex);
        }
        return items.get(rowIndex);
    }
}
