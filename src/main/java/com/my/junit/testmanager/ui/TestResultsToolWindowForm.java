package com.my.junit.testmanager.ui;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.my.junit.testmanager.data.TestResultRowData;
import com.my.junit.testmanager.data.TestResultsSummary;
import com.my.junit.testmanager.model.TestResultsTableModel;
import com.my.junit.testmanager.ui.render.StatusTableCellRenderer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Форма для отображения результатов тестов в кастомном tool window.
 */
@Getter
public class TestResultsToolWindowForm {
    private final Project project;
    private final ConcurrentMap<String, PsiClass> classCache = new ConcurrentHashMap<>();
    private JPanel panel;
    private JPanel summaryPanel;
    private JLabel totalDurationLabel;
    private JLabel passedLabel;
    private JLabel warningLabel;
    private JLabel failedLabel;
    private JBCheckBox regressionsOnlyCheckBox;
    private JBCheckBox showTotalsCheckBox;
    private JPanel statusFilterPanel;
    private JCheckBox successCheckBox;
    private JCheckBox warningCheckBox;
    private JCheckBox failedCheckBox;
    private JCheckBox ignoredCheckBox;
    private JBTable resultsTable;
    private JSplitPane detailsSplitPane;
    private JPanel currentLogPanel;
    private JLabel currentLogLabel;
    private JPanel currentLogContainer;
    private JPanel previousLogPanel;
    private JLabel previousLogLabel;
    private JPanel previousLogContainer;

    private TestResultsTableModel tableModel;
    private TableRowSorter<TestResultsTableModel> sorter;
    private JBTextArea currentLogArea;
    private JBTextArea previousLogArea;

    public TestResultsToolWindowForm(@NotNull Project project) {
        this.project = project;
        this.tableModel = new TestResultsTableModel(Collections.emptyList());
        this.resultsTable.setModel(this.tableModel);
        this.resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.sorter = new TableRowSorter<>(tableModel);
        this.resultsTable.setRowSorter(sorter);
        this.resultsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusTableCellRenderer());

        this.totalDurationLabel.setText(message("toolwindow.results.summary.total", "0 ms"));
        this.passedLabel.setText(message("toolwindow.results.summary.passed", 0));
        this.warningLabel.setText(message("toolwindow.results.summary.warning", 0));
        this.failedLabel.setText(message("toolwindow.results.summary.failed", 0));
        this.regressionsOnlyCheckBox.setText(message("toolwindow.results.filter.regressions"));
        this.currentLogLabel.setText(message("toolwindow.results.log.current"));
        this.previousLogLabel.setText(message("toolwindow.results.log.previous"));
        this.regressionsOnlyCheckBox.addActionListener(e -> applyFilter());
        this.showTotalsCheckBox = new JBCheckBox(
                message("toolwindow.results.filter.show.totals"),
                true
        );
        this.showTotalsCheckBox.addActionListener(e -> applyFilter());
        this.summaryPanel.add(this.showTotalsCheckBox);
        initStatusFilters();

        initLogAreas();
        initInteractions();
    }

    public void setResults(@NotNull List<TestResultRowData> rows) {
        this.tableModel.setItems(rows);
        applyFilter();
        clearLogAreas();
    }

    public void updateSummary(@NotNull TestResultsSummary summary) {
        this.totalDurationLabel.setText(
                message("toolwindow.results.summary.total", summary.getTotalDuration()));
        this.passedLabel.setText(message("toolwindow.results.summary.passed", summary.getPassedCount()));
        this.warningLabel.setText(
                message("toolwindow.results.summary.warning", summary.getWarningCount()));
        this.failedLabel.setText(message("toolwindow.results.summary.failed", summary.getFailedCount()));
    }

    private void initLogAreas() {
        this.currentLogArea = createLogArea();
        this.previousLogArea = createLogArea();

        this.currentLogContainer.removeAll();
        this.currentLogContainer.setLayout(new BorderLayout());
        this.currentLogContainer.add(ScrollPaneFactory.createScrollPane(currentLogArea), BorderLayout.CENTER);

        this.previousLogContainer.removeAll();
        this.previousLogContainer.setLayout(new BorderLayout());
        this.previousLogContainer.add(ScrollPaneFactory.createScrollPane(previousLogArea), BorderLayout.CENTER);

        clearLogAreas();
    }

    private JBTextArea createLogArea() {
        final var area = new JBTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private void initInteractions() {
        this.resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    final int viewRow = resultsTable.rowAtPoint(e.getPoint());
                    if (viewRow == -1) {
                        return;
                    }
                    resultsTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    final int modelRow = resultsTable.convertRowIndexToModel(viewRow);
                    final var rowData = tableModel.getItemAt(modelRow);
                    final int viewColumn = resultsTable.columnAtPoint(e.getPoint());
                    if (viewColumn == 1 && !rowData.isSummaryRow()) {
                        navigateToMethod(rowData);
                    } else {
                        showLogs(rowData);
                    }
                }
            }
        });
        ActionListener filterListener = e -> applyFilter();
        successCheckBox.addActionListener(filterListener);
        warningCheckBox.addActionListener(filterListener);
        failedCheckBox.addActionListener(filterListener);
        ignoredCheckBox.addActionListener(filterListener);
    }

    private void clearLogAreas() {
        final var message = message("toolwindow.results.log.no.selection");
        currentLogArea.setText(message);
        previousLogArea.setText(message);
        currentLogArea.setCaretPosition(0);
        previousLogArea.setCaretPosition(0);
    }

    private void applyFilter() {
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TestResultsTableModel, ? extends Integer> entry) {
                final var row = tableModel.getItemAt(entry.getIdentifier());
                if (row.isSummaryRow()) {
                    // Итоговые строки всегда скрываем, если отключен показ итогов
                    if (showTotalsCheckBox != null && !showTotalsCheckBox.isSelected()) {
                        return false;
                    }

                    // Показываем итог только для тех классов, у которых есть хотя бы одна видимая строка
                    final var className = row.getTestClassName();
                    if (className == null || className.isBlank()) {
                        return false;
                    }

                    return tableModel.getItems().stream()
                            .filter(r -> !r.isSummaryRow())
                            .filter(r -> className.equals(r.getTestClassName()))
                            .anyMatch(r -> {
                                if (!isStatusAllowed(r.getCurrentResult())) {
                                    return false;
                                }
                                if (regressionsOnlyCheckBox.isSelected()) {
                                    final var previous = r.getPreviousResult();
                                    return previous != null && !previous.equals(r.getCurrentResult());
                                }
                                return true;
                            });
                }
                if (!isStatusAllowed(row.getCurrentResult())) {
                    return false;
                }
                if (regressionsOnlyCheckBox.isSelected()) {
                    final var previous = row.getPreviousResult();
                    return previous != null && !previous.equals(row.getCurrentResult());
                }
                return true;
            }
        });
    }

    private boolean isStatusAllowed(String status) {
        if (status == null) {
            return true;
        }
        final var normalized = status.toLowerCase();
        if (normalized.contains("успех") || normalized.contains("success")) {
            return successCheckBox.isSelected();
        }
        if (normalized.contains("внимание") || normalized.contains("warning")) {
            return warningCheckBox.isSelected();
        }
        if (normalized.contains("ошибка") || normalized.contains("failed") || normalized.contains("error")) {
            return failedCheckBox.isSelected();
        }
        if (normalized.contains("пропущ") || normalized.contains("ignored")) {
            return ignoredCheckBox.isSelected();
        }
        return true;
    }

    private void initStatusFilters() {
        successCheckBox = new JCheckBox(message("toolwindow.results.filter.success"), true);
        warningCheckBox = new JCheckBox(message("toolwindow.results.filter.warning"), true);
        failedCheckBox = new JCheckBox(message("toolwindow.results.filter.failed"), true);
        ignoredCheckBox = new JCheckBox(message("toolwindow.results.filter.ignored"), true);
        statusFilterPanel.removeAll();
        statusFilterPanel.add(successCheckBox);
        statusFilterPanel.add(warningCheckBox);
        statusFilterPanel.add(failedCheckBox);
        statusFilterPanel.add(ignoredCheckBox);
    }

    private void showLogs(TestResultRowData rowData) {
        if (rowData.isSummaryRow()) {
            clearLogAreas();
            return;
        }
        currentLogArea.setText(
                rowData.getLogOutput() != null ? rowData.getLogOutput()
                        : message("toolwindow.results.log.empty")
        );
        previousLogArea.setText(
                rowData.getPreviousLogOutput() != null ? rowData.getPreviousLogOutput()
                        : message("toolwindow.results.log.empty")
        );
        currentLogArea.setCaretPosition(0);
        previousLogArea.setCaretPosition(0);
    }

    private void navigateToMethod(TestResultRowData row) {
        if (row.isSummaryRow() || project.isDisposed()) {
            return;
        }
        ReadAction
                .nonBlocking(() -> findNavigationTarget(row))
                .expireWith(project)
                .inSmartMode(project)
                .finishOnUiThread(ModalityState.NON_MODAL, target -> {
                    if (target == null || target.getContainingFile() == null ||
                            target.getContainingFile().getVirtualFile() == null) {
                        return;
                    }
                    PsiNavigationSupport.getInstance()
                            .createNavigatable(project, target.getContainingFile().getVirtualFile(),
                                    target.getTextOffset())
                            .navigate(true);
                })
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    private PsiElement findNavigationTarget(TestResultRowData row) {
        final var psiClass = findPsiClass(row);
        if (psiClass == null) {
            return null;
        }
        PsiElement target = findPsiMethod(psiClass, row.getNormalizedMethodName());
        if (target == null) {
            target = psiClass;
        }
        return target.getNavigationElement() != null ? target.getNavigationElement() : target;
    }

    private PsiClass findPsiClass(TestResultRowData row) {
        final var scope = GlobalSearchScope.allScope(project);
        final var facade = JavaPsiFacade.getInstance(project);
        final var qualified = row.getClassQualifiedName();
        if (qualified != null && !qualified.isBlank()) {
            for (var candidate : expandQualifiedNames(qualified)) {
                final var resolved = resolveAndCacheClass(candidate, facade, scope);
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        var shortName = row.getTestClassName();
        if (shortName == null || shortName.isBlank()) {
            return null;
        }
        final var slashIdx = shortName.indexOf('/');
        if (slashIdx > 0) {
            shortName = shortName.substring(0, slashIdx);
        }
        shortName = shortName.trim();
        if (shortName.isEmpty()) {
            return null;
        }
        for (var candidate : expandShortNames(shortName)) {
            final var candidates = PsiShortNamesCache.getInstance(project)
                    .getClassesByName(candidate, scope);
            if (candidates.length > 0) {
                return candidates[0];
            }
        }
        return null;
    }

    private PsiClass resolveAndCacheClass(@NotNull String qualified,
            @NotNull JavaPsiFacade facade,
            @NotNull GlobalSearchScope scope) {
        final var cached = classCache.get(qualified);
        if (cached != null && cached.isValid()) {
            return cached;
        }
        final var resolved = facade.findClass(qualified, scope);
        if (resolved != null) {
            classCache.put(qualified, resolved);
        }
        return resolved;
    }

    private List<String> expandQualifiedNames(@NotNull String original) {
        final var variants = new LinkedHashSet<String>();
        variants.add(original);
        final var normalized = original.replace('$', '.');
        if (!normalized.equals(original)) {
            variants.add(normalized);
        }
        return new ArrayList<>(variants);
    }

    private List<String> expandShortNames(@NotNull String original) {
        final var variants = new LinkedHashSet<String>();
        variants.add(original);
        final int dollarIdx = original.lastIndexOf('$');
        if (dollarIdx >= 0 && dollarIdx + 1 < original.length()) {
            variants.add(original.substring(dollarIdx + 1));
        }
        return new ArrayList<>(variants);
    }

    private PsiElement findPsiMethod(@NotNull PsiClass psiClass, @NotNull String methodName) {
        final var normalized = methodName.trim();
        if (normalized.isEmpty() ||
                normalized.equalsIgnoreCase(message("toolwindow.results.row.total"))) {
            return psiClass;
        }
        final var methods = psiClass.findMethodsByName(normalized, true);
        if (methods.length > 0) {
            return methods[0];
        }
        return psiClass;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.add(summaryPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalDurationLabel = new JLabel();
        totalDurationLabel.setText("Total time: 0 ms");
        summaryPanel.add(totalDurationLabel);
        passedLabel = new JLabel();
        passedLabel.setText("Passed: 0");
        summaryPanel.add(passedLabel);
        warningLabel = new JLabel();
        warningLabel.setText("Warning: 0");
        summaryPanel.add(warningLabel);
        failedLabel = new JLabel();
        failedLabel.setText("Failed: 0");
        summaryPanel.add(failedLabel);
        regressionsOnlyCheckBox = new JBCheckBox();
        regressionsOnlyCheckBox.setText("Show regressions only");
        summaryPanel.add(regressionsOnlyCheckBox);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        statusFilterPanel = new JPanel();
        statusFilterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel1.add(statusFilterPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusFilterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                        0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        resultsTable = new JBTable();
        scrollPane1.setViewportView(resultsTable);
        detailsSplitPane = new JSplitPane();
        detailsSplitPane.setDividerLocation(400);
        panel.add(detailsSplitPane,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        currentLogPanel = new JPanel();
        currentLogPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 5));
        detailsSplitPane.setLeftComponent(currentLogPanel);
        currentLogLabel = new JLabel();
        currentLogLabel.setText("Current output");
        currentLogPanel.add(currentLogLabel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentLogContainer = new JPanel();
        currentLogContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        currentLogPanel.add(currentLogContainer,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        currentLogContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        previousLogPanel = new JPanel();
        previousLogPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 5));
        detailsSplitPane.setRightComponent(previousLogPanel);
        previousLogLabel = new JLabel();
        previousLogLabel.setText("Previous output");
        previousLogPanel.add(previousLogLabel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        previousLogContainer = new JPanel();
        previousLogContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        previousLogPanel.add(previousLogContainer,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        previousLogContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return panel;}

}


