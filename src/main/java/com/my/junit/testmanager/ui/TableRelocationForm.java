package com.my.junit.testmanager.ui;

import com.intellij.openapi.project.Project;
import com.my.junit.testmanager.data.TestClassRelocationData;
import com.my.junit.testmanager.model.TestClassRelocationTableModel;
import com.my.junit.testmanager.render.CheckboxRenderer;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.MessagesBundle;
import com.my.junit.testmanager.utils.MessagesDialogUtils;
import com.my.junit.testmanager.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;
import static com.my.junit.testmanager.utils.PsiUtils.openPsiClassForEditor;

/**
 * Форма таблицы для релокации тестовых классов.
 */
public class TableRelocationForm extends AbstractTableForm<TestClassRelocationTableModel> {
    private final LoggerUtils log = LoggerUtils.getLogger(TableRelocationForm.class);

    private final Project project;

    public TableRelocationForm(
            @NotNull TestClassRelocationTableModel model,
            @NotNull Project project
    ) {
        super(
                message("dialog.title.relocate.tests"),
                message("button.relocate"),
                message("button.cancel"),
                model
        );
        this.project = project;
    }

    @Override
    protected void initForm() {
        SwingUtilities.invokeLater(() -> {
            if (getWindow() != null) {
                getWindow().setVisible(true);
                getWindow().toFront();
                getWindow().requestFocusInWindow();
            }
        });
    }

    @Override
    protected void initTable() {
        initCheckBox();

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        sorter.setSortable(0, false);

        table.addMouseListener(doubleClickRow());
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
        return RowFilter.regexFilter("(?i)" + query, 1);
    }

    @Override
    protected void doOKAction() {
        final var selected = getSelectedItems();
        if (selected.isEmpty()) {
            MessagesDialogUtils.messageWarn(
                    project,
                    MessagesBundle.message("dialog.relocation.no.selected")
            );
            return;
        }
        log.logInfo("User selected " + selected.size() + " tests for relocation.");
        for (var item : selected) {
            log.logInfo("Selected item: " + item);
            PsiUtils.movePsiClass(
                    project,
                    item.getPsiClass(),
                    item.getNewPath()
            );
        }
        log.logInfo("Relocation complete. Total tests moved");
        MessagesDialogUtils.messageInfo(
                project,
                message("dialog.relocation.completed", selected.size())
        );
        super.doOKAction();
    }

    private void initCheckBox() {
        final var checkboxColumn = table.getColumnModel().getColumn(0);
        checkboxColumn.setMaxWidth(50);
        checkboxColumn.setCellRenderer(new CheckboxRenderer());
        checkboxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));

        final var header = table.getTableHeader();
        header.addMouseListener(clickHeaderCheckBox());
    }

    @NotNull
    private MouseAdapter clickHeaderCheckBox() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int column = table.columnAtPoint(e.getPoint());
                    if (column == 0) {
                        boolean allSelected =
                                tableModel.getItems()
                                        .stream()
                                        .allMatch(TestClassRelocationData::isSelected);

                        tableModel.getItems().forEach(item -> item.setSelected(!allSelected));
                        tableModel.fireTableDataChanged();
                    }
                }
            }
        };
    }

    private MouseAdapter doubleClickRow() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && column == 1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        final var item = tableModel.getItems().get(modelRow);
                        openPsiClassForEditor(project, item.getPsiClass());
                    }
                }
            }
        };
    }

    private List<TestClassRelocationData> getSelectedItems() {
        return tableModel.getItems()
                .stream()
                .filter(TestClassRelocationData::isSelected)
                .toList();
    }

}
