package com.my.junit.testmanager.model;

import com.my.junit.testmanager.config.data.GroupData;
import com.my.junit.testmanager.config.data.ProfileData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Модель таблицы для отображения групп настроек.
 */
public class GroupConfigTableModel extends AbstractBaseTableModel<GroupData> {

    private final String[] columnNames = {
            message("settings.group.table.column.name"),
            message("settings.group.table.column.regex"),
            message("settings.group.table.column.vmArgs"),
            message("settings.group.table.column.color"),
            message("settings.group.table.column.profiles")
    };

    public GroupConfigTableModel(@NotNull List<GroupData> items) {
        super(items);
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
                    .map(ProfileData::getName)
                    .collect(Collectors.joining(", "));
            default -> null;
        };
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

}

