package com.my.junit.testmanager.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.my.junit.testmanager.config.TestGeneratorConfig;
import com.my.junit.testmanager.model.AnnotationsTableModel;
import com.my.junit.testmanager.model.FactoryMethodTableModel;
import com.my.junit.testmanager.utils.LoggerUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

public class SettingsTestGeneratorForm implements Configurable {
    private AnnotationsTableModel annotationsTableModel;
    private FactoryMethodTableModel factoryMethodTableModel;
    private JTextField textFieldGeneratorClassName;
    private JLabel labelGeneratorClassName;
    private JLabel labelGeneratorClassMethodName;
    private JTextField textFieldGeneratorClassMethodName;
    private JTable tableAnnotations;
    private JTable tableFactorys;
    private JButton buttonAnnotationAdd;
    private JButton buttonAnnotationRemove;
    private JButton buttonFactoryAdd;
    private JButton buttonFactoryRemove;
    private JPanel table;

    private final LoggerUtils log = LoggerUtils.getLogger(SettingsConfigurationForm.class);
    private TestGeneratorConfig testGeneratorSettings;

    public SettingsTestGeneratorForm(@NotNull Project project) {
        this.testGeneratorSettings = TestGeneratorConfig.getInstance(project);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Settings Test Generator";
    }

    @Override
    @Nullable
    public JComponent createComponent() {

        this.labelGeneratorClassMethodName.setText(message("settings.label.prefix.method.name.generator"));
        this.labelGeneratorClassName.setText(message("setting.label.prefix.class.name.generator"));

        this.buttonAnnotationAdd.setText(message("button.add"));
        this.buttonAnnotationAdd.addActionListener(e -> addAnnotation());

        this.buttonAnnotationRemove.setText(message("button.remove"));
        this.buttonAnnotationRemove.addActionListener(e -> removeAnnotation());

        this.buttonFactoryAdd.setText(message("button.add"));
        this.buttonFactoryAdd.addActionListener(e -> addFactoryMethod());

        this.buttonFactoryRemove.setText(message("button.remove"));
        this.buttonFactoryRemove.addActionListener(e -> removeFactoryMethod());

        this.annotationsTableModel = new AnnotationsTableModel(testGeneratorSettings.getAnnotationsDataList());
        this.tableAnnotations.setModel(this.annotationsTableModel);

        this.factoryMethodTableModel = new FactoryMethodTableModel(testGeneratorSettings.getFactoryMethodDataList());
        this.tableFactorys.setModel(this.factoryMethodTableModel);

        this.textFieldGeneratorClassMethodName.setText(testGeneratorSettings.getGeneratorMethodName());
        this.textFieldGeneratorClassName.setText(testGeneratorSettings.getGeneratorClassName());

        log.logInfo("Create Settings Test Generator UI component");
        return table;
    }

    @Override
    public boolean isModified() {
        final var settings = getSettings();
        log.logInfo("Checking if settings modified. Current: " + testGeneratorSettings + ", New: " + settings);
        return !testGeneratorSettings.isStateEquals(settings);
    }

    @Override
    public void apply() {
        final var settings = getSettings();
        XmlSerializerUtil.copyBean(settings, testGeneratorSettings);
        log.logInfo("Settings applied: " + testGeneratorSettings);
    }

    @Override
    public void reset() {
        this.textFieldGeneratorClassName.setText(this.testGeneratorSettings.getGeneratorClassName());
        this.textFieldGeneratorClassMethodName.setText(this.testGeneratorSettings.getGeneratorMethodName());
        this.annotationsTableModel.setItems(
                this.testGeneratorSettings.getAnnotationsDataList()
        );
        this.factoryMethodTableModel.setItems(
                this.testGeneratorSettings.getFactoryMethodDataList()
        );
        log.logInfo("Settings reset to: " + this.testGeneratorSettings);
    }

    private void addAnnotation() {
        final var dialog = new AnnotationsForm();
        if (dialog.showAndGet()) {
            final var data = dialog.getObject();
            this.annotationsTableModel.addRow(data);
            log.logInfo("Added new annotation data: " + data);
        }
    }

    private void removeAnnotation() {
        final var selectedRow = this.tableAnnotations.getSelectedRow();
        if (selectedRow >= 0) {
            final var data = this.annotationsTableModel.getItems().get(selectedRow);
            this.annotationsTableModel.removeRow(selectedRow);
            log.logInfo("Removed annotation data: " + data);
        }
    }

    private void addFactoryMethod() {
        final var dialog = new FactoryMethodForm();
        if (dialog.showAndGet()) {
            final var data = dialog.getObject();
            this.factoryMethodTableModel.addRow(data);
            log.logInfo("Added new factory method data: " + data);
        }
    }

    private void removeFactoryMethod() {
        final var selectedRow = this.tableFactorys.getSelectedRow();
        if (selectedRow >= 0) {
            final var data = this.factoryMethodTableModel.getItems().get(selectedRow);
            this.factoryMethodTableModel.removeRow(selectedRow);
            log.logInfo("Removed factory method data: " + data);
        }
    }


    private TestGeneratorConfig getSettings() {
        final var settings = new TestGeneratorConfig();
        settings.setGeneratorClassName(this.textFieldGeneratorClassName.getText());
        settings.setGeneratorMethodName(this.textFieldGeneratorClassMethodName.getText());
        settings.setAnnotationsDataList(this.annotationsTableModel.getItems());
        settings.setFactoryMethodDataList(this.factoryMethodTableModel.getItems());
        return settings;
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
        table = new JPanel();
        table.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout(
                "fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:d:grow",
                "center:d:noGrow"));
        table.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                new Dimension(446, 76), null, 0, false));
        labelGeneratorClassName = new JLabel();
        labelGeneratorClassName.setText("Prefix Name Generator Class");
        CellConstraints cc = new CellConstraints();
        panel1.add(labelGeneratorClassName, cc.xy(1, 1));
        textFieldGeneratorClassName = new JTextField();
        panel1.add(textFieldGeneratorClassName, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        labelGeneratorClassMethodName = new JLabel();
        labelGeneratorClassMethodName.setText("Prefix Name Generator Class Method ");
        panel1.add(labelGeneratorClassMethodName, cc.xy(5, 1));
        textFieldGeneratorClassMethodName = new JTextField();
        panel1.add(textFieldGeneratorClassMethodName, cc.xy(7, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow"));
        table.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, cc.xy(1, 1));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Annotation", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                        0, false));
        tableAnnotations = new JTable();
        scrollPane1.setViewportView(tableAnnotations);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        buttonAnnotationAdd = new JButton();
        buttonAnnotationAdd.setText("Add");
        panel4.add(buttonAnnotationAdd);
        buttonAnnotationRemove = new JButton();
        buttonAnnotationRemove.setText("Remove");
        panel4.add(buttonAnnotationRemove);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, cc.xy(3, 1));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Factory", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                        0, false));
        tableFactorys = new JTable();
        scrollPane2.setViewportView(tableFactorys);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel5.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        buttonFactoryAdd = new JButton();
        buttonFactoryAdd.setText("Add");
        panel6.add(buttonFactoryAdd);
        buttonFactoryRemove = new JButton();
        buttonFactoryRemove.setText("Remove");
        panel6.add(buttonFactoryRemove);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return table;}
}
