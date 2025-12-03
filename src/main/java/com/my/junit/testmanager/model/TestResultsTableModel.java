package com.my.junit.testmanager.model;

import com.my.junit.testmanager.data.TestResultRowData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Табличная модель для отображения результатов тестов.
 */
public class TestResultsTableModel extends AbstractBaseTableModel<TestResultRowData> {

    private final String[] columnNames = new String[]{
            message("toolwindow.results.columns.class"),
            message("toolwindow.results.columns.method"),
            message("toolwindow.results.columns.current"),
            message("toolwindow.results.columns.previous"),
            message("toolwindow.results.columns.duration")
    };

    public TestResultsTableModel(@NotNull List<TestResultRowData> items) {
        super(items);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final var row = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getDisplayTestClassName();
            case 1 -> row.getMethodName();
            case 2 -> row.getCurrentResult();
            case 3 -> row.getPreviousResult();
            case 4 -> row.getDurationFormatted();
            default -> "";
        };
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

