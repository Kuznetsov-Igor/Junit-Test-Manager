package com.my.junit.testmanager.ui;

import com.intellij.openapi.project.Project;
import com.my.junit.testmanager.config.data.GroupData;
import com.my.junit.testmanager.data.TestClassInfoData;
import com.my.junit.testmanager.model.TestClassInfoTableModel;
import com.my.junit.testmanager.render.GroupTableCellRenderer;
import com.my.junit.testmanager.utils.JunitTestConfigurationUtils;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.MessagesDialogUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Форма таблицы для генерации конфигураций тестов.
 */
public class TestConfigurationsForm extends AbstractTableForm<TestClassInfoTableModel> {
    private final LoggerUtils log = LoggerUtils.getLogger(TestConfigurationsForm.class);

    private final Project project;

    public TestConfigurationsForm(
            @NotNull TestClassInfoTableModel model,
            @NotNull Project project
    ) {
        super(
                "Relocate Tests",
                message("button.test.configurations"),
                message("button.cancel"),
                model
        );
        this.project = project;
    }


    @Override
    protected void initForm() {
        // No specific form initialization needed
    }

    @Override
    protected void initTable() {
        table.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        new GroupTableCellRenderer()
                );

        sorter.setComparator(2, (o1, o2) -> {
            if (o1 instanceof GroupData g1 && o2 instanceof GroupData g2) {
                return g1.getName().compareTo(g2.getName());
            }
            return 0;
        });
    }

    @Override
    @NotNull
    protected String getCopyText(int rowIndex) {
        final var value = tableModel.getValueAt(rowIndex, 1);
        return value != null ? value.toString() : "";
    }

    @Override
    @NotNull
    protected <M, I> RowFilter<M, I> getRowFilter(@NotNull String query) {
        return RowFilter.regexFilter("(?i)" + query, 0);
    }

    @Override
    protected void doOKAction() {
        int[] selectedRows = table.getSelectedRows();
        var selectedTests = new ArrayList<TestClassInfoData>();

        if (selectedRows.length == 0) {
            log.logInfo("No rows selected, processing all items.");
            selectedTests = new ArrayList<>(tableModel.getItems());
        } else {
            log.logInfo("Processing " + selectedRows.length + " selected rows.");
            for (int row : selectedRows) {
                int modelRow = table.convertRowIndexToModel(row);
                final var item = tableModel.getItems().get(modelRow);
                if (item != null) {
                    selectedTests.add(item);
                }
            }
        }
        for (var entry : selectedTests.stream()
                .collect(
                        Collectors.groupingBy(
                                TestClassInfoData::getGroup,
                                Collectors.toList()
                        )
                ).entrySet()
        ) {
            JunitTestConfigurationUtils.createJunitConfigurationTest(
                    project,
                    entry.getValue()
                            .stream()
                            .map(TestClassInfoData::getPsiClass)
                            .toList(),
                    entry.getKey()
            );
        }

        MessagesDialogUtils.messageInfo(
                project,
                message("dialog.test.configurations.generated")
        );
        super.doOKAction();
    }
}




