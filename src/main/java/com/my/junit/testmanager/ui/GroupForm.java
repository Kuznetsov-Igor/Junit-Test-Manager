package com.my.junit.testmanager.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.my.junit.testmanager.config.TestManagerConfig;
import com.my.junit.testmanager.config.data.GroupData;
import com.my.junit.testmanager.config.data.ProfileData;
import com.my.junit.testmanager.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.my.junit.testmanager.utils.MessagesBundle.message;
import static javax.swing.BorderFactory.createTitledBorder;

/**
 * Диалоговое окно для добавления и редактирования групп профилей.
 */
public class GroupForm extends DialogWrapper {
    private JTextField textFieldName;
    private JLabel labelName;
    private JLabel labelRegex;
    private JTextField textFieldRegex;
    private JLabel labelVmArg;
    private JTextField textFieldVmArg;
    private JPanel panelColorChooser;
    private JLabel labelColor;
    private JButton addProfileButton;
    private JButton removeProfileButton;
    private JLabel labelAvailable;
    private JList availableProfilesList;
    private JLabel labelSelected;
    private JList selectedProfilesList;
    private JPanel profilesPanel;
    private JColorChooser colorChooser;
    private JPanel panel;

    private DefaultListModel<ProfileData> availableModel;
    private DefaultListModel<ProfileData> selectedModel;
    private GroupData existingGroup;

    private final LoggerUtils log = LoggerUtils.getLogger(GroupForm.class);

    public GroupForm(@Nullable GroupData existing) {
        super(true);
        this.existingGroup = existing;
        setTitle(existing == null ? message("dialog.title.add") : message("dialog.title.edit"));
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        this.labelName.setText(message("settings.group.label.name"));
        this.labelName.setToolTipText(message("settings.group.label.name.tooltip"));

        this.labelRegex.setText(message("settings.group.label.regex"));
        this.labelRegex.setToolTipText(message("settings.group.label.regex.tooltip"));

        this.labelVmArg.setText(message("settings.group.label.vmArgs"));
        this.labelVmArg.setToolTipText(message("settings.group.label.vmArgs.tooltip"));

        this.labelColor.setText(message("settings.group.label.color"));
        this.labelColor.setToolTipText(message("settings.group.label.color.tooltip"));

        this.labelAvailable.setText(message("settings.group.label.available.profiles"));
        this.labelSelected.setText(message("settings.group.label.selected.profiles"));

        this.profilesPanel.setBorder(createTitledBorder(message("settings.group.label.profiles")));

        if (this.existingGroup != null) {
            this.textFieldName.setText(this.existingGroup.getName());
            this.textFieldRegex.setText(this.existingGroup.getRegex());
            this.textFieldVmArg.setText(this.existingGroup.getVmArgs());
            this.colorChooser.setColor(this.existingGroup.getColor());
        } else {
            this.colorChooser.setColor(Color.BLUE);
        }

        final var allProfiles = TestManagerConfig.getInstance().getProfiles();

        this.availableModel = new DefaultListModel<>();
        this.selectedModel = new DefaultListModel<>();

        final var selectedProfiles = this.existingGroup != null ? new ArrayList<>(this.existingGroup.getProfiles())
                : new ArrayList<ProfileData>();
        for (var profile : allProfiles) {
            if (selectedProfiles.contains(profile)) {
                this.selectedModel.addElement(profile);
            } else {
                this.availableModel.addElement(profile);
            }
        }

        this.availableProfilesList.setModel(this.availableModel);
        this.selectedProfilesList.setModel(this.selectedModel);

        this.availableProfilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.selectedProfilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        this.addProfileButton.addActionListener(
                e -> moveProfiles(this.availableProfilesList, this.availableModel, this.selectedModel));
        this.removeProfileButton.addActionListener(
                e -> moveProfiles(this.selectedProfilesList, this.selectedModel, this.availableModel));

        log.logInfo("GroupEdit UI initialized.");
        return this.panel;
    }

    private void moveProfiles(
            @NotNull JList<ProfileData> fromList,
            @NotNull DefaultListModel<ProfileData> fromModel,
            @NotNull DefaultListModel<ProfileData> toModel
    ) {
        int[] selectedIndices = fromList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            return;
        }

        Arrays.sort(selectedIndices);
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            final var profile = fromModel.getElementAt(selectedIndices[i]);
            fromModel.removeElementAt(selectedIndices[i]);
            toModel.addElement(profile);
        }
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        final var name = this.textFieldName.getText().trim();
        if (name.isEmpty()) {
            return new ValidationInfo(message("validation.error.group.name.empty"), this.textFieldName);
        }

        final var regex = this.textFieldRegex.getText().trim();
        if (!regex.isEmpty()) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                return new ValidationInfo(
                        message("validation.error.regex.invalid", e.getMessage()),
                        this.textFieldRegex
                );
            }
        }

        return null;
    }

    public GroupData getGroup() {
        List<ProfileData> selectedProfiles = new ArrayList<>();
        for (int i = 0; i < this.selectedModel.getSize(); i++) {
            selectedProfiles.add(this.selectedModel.getElementAt(i));
        }

        final var group = GroupData.of(
                this.textFieldName.getText(),
                this.textFieldRegex.getText(),
                this.textFieldVmArg.getText(),
                this.colorChooser.getColor(),
                selectedProfiles
        );

        log.logInfo("Group data collected: " + group);
        return group;
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        labelName = new JLabel();
        labelName.setText("Label");
        panel2.add(labelName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldName = new JTextField();
        panel2.add(textFieldName,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        labelRegex = new JLabel();
        labelRegex.setText("Label");
        panel3.add(labelRegex, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldRegex = new JTextField();
        panel3.add(textFieldRegex,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        labelVmArg = new JLabel();
        labelVmArg.setText("Label");
        panel4.add(labelVmArg, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldVmArg = new JTextField();
        panel4.add(textFieldVmArg,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null, 0, false));
        panelColorChooser = new JPanel();
        panelColorChooser.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panelColorChooser,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        labelColor = new JLabel();
        labelColor.setText("Label");
        panelColorChooser.add(labelColor,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        colorChooser = new JColorChooser();
        panelColorChooser.add(colorChooser,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        profilesPanel = new JPanel();
        profilesPanel.setLayout(new BorderLayout(0, 0));
        panel.add(profilesPanel,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        profilesPanel.setBorder(BorderFactory.createTitledBorder(null, "Profiles", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        profilesPanel.add(panel5, BorderLayout.CENTER);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        labelAvailable = new JLabel();
        labelAvailable.setText("Label");
        panel6.add(labelAvailable,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel6.add(scrollPane1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                        0, false));
        availableProfilesList = new JList();
        scrollPane1.setViewportView(availableProfilesList);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        addProfileButton = new JButton();
        addProfileButton.setText(">>");
        panel7.add(addProfileButton,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, 25), new Dimension(50, 25),
                        new Dimension(50, 25), 0, false));
        removeProfileButton = new JButton();
        removeProfileButton.setText("<<");
        panel7.add(removeProfileButton,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, 25), new Dimension(50, 25),
                        new Dimension(50, 25), 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel8, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        labelSelected = new JLabel();
        labelSelected.setText("Label");
        panel8.add(labelSelected,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel8.add(scrollPane2,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                        0, false));
        selectedProfilesList = new JList();
        scrollPane2.setViewportView(selectedProfilesList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return panel;}
}
