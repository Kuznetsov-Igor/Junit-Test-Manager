package com.my.junit.testmanager.model;

import com.my.junit.testmanager.config.data.GroupConfigData;
import com.my.junit.testmanager.config.data.ProfileConfigData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения групп настроек.
 */
public class GroupConfigTableModel extends AbstractTableModel {
    @Getter
    private final List<GroupConfigData> items;
    private final String[] columnNames = {
            message("settings.group.table.column.name"),
            message("settings.group.table.column.regex"),
            message("settings.group.table.column.vmArgs"),
            message("settings.group.table.column.color"),
            message("settings.group.table.column.profiles")
    };

    public GroupConfigTableModel(@NotNull List<GroupConfigData> items) {
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
            case 1 -> item.getRegex();
            case 2 -> item.getVmArgs();
            case 3 -> item.getHexColor();
            case 4 -> item.getProfiles()
                    .stream()
                    .map(ProfileConfigData::getName)
                    .collect(Collectors.joining(", "));
            default -> null;
        };
    }

    public void addRow(@NotNull GroupConfigData item) {
        items.add(item);
        fireTableRowsInserted(items.size() - 1, items.size() - 1);
    }

    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            items.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void updateRow(int rowIndex, @NotNull GroupConfigData item) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            items.set(rowIndex, item);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

}

