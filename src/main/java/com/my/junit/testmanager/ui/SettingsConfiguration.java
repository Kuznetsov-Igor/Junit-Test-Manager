package com.my.junit.testmanager.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.my.junit.testmanager.config.TestManagerSettings;
import com.my.junit.testmanager.data.Language;
import com.my.junit.testmanager.model.GroupConfigTableModel;
import com.my.junit.testmanager.utils.LoggerUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Конфигурация настроек плагина в настройках IDE.
 */
public class SettingsConfiguration implements Configurable {
    private JPanel panel;
    private JLabel languageLabel;
    private JComboBox<String> languageCombo;
    private JCheckBox loggingCheckBox;
    private JLabel activeProfileLabel;
    private JComboBox<String> profilesCombo;
    private JButton addProfileButton;
    private JButton editProfileButton;
    private JButton removeProfileButton;
    private JTable groupsTable;
    private JButton addGroupButton;
    private JButton editGroupButton;
    private JButton removeGroupButton;

    private GroupConfigTableModel groupConfigTableModel;
    private TestManagerSettings currentSettings = new TestManagerSettings();
    private final LoggerUtils log = LoggerUtils.getLogger(SettingsConfiguration.class);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Test Manager Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        this.languageLabel.setText(message("settings.label.language"));
        this.languageLabel.setToolTipText(message("settings.label.language.tooltip"));

        this.loggingCheckBox.setText(message("settings.label.logger"));
        this.loggingCheckBox.setToolTipText(message("settings.label.logger.tooltip"));

        this.activeProfileLabel.setText(message("settings.label.active.profile"));
        this.addProfileButton.setText(message("button.add"));
        this.editProfileButton.setText(message("button.edit"));
        this.removeProfileButton.setText(message("button.remove"));
        this.addGroupButton.setText(message("button.add"));
        this.editGroupButton.setText(message("button.edit"));
        this.removeGroupButton.setText(message("button.remove"));

        Arrays.stream(Language.values()).forEach(lang ->
                this.languageCombo.addItem(lang.getDisplayName())
        );
        this.languageCombo.setSelectedItem(
                Language.getLocaleFromDisplay(currentSettings.getLanguageName()).getDisplayName()
        );
        currentSettings.getProfiles().forEach(profile ->
                this.profilesCombo.addItem(profile.getName())
        );
        this.profilesCombo.setSelectedItem(currentSettings.getActiveProfile().getName());
        this.loggingCheckBox.setSelected(currentSettings.isLoggingEnabled());
        this.groupConfigTableModel = new GroupConfigTableModel(currentSettings.getGroups());
        this.groupsTable.setModel(groupConfigTableModel);
        this.groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.addGroupButton.addActionListener(e -> addGroup());
        this.editGroupButton.addActionListener(e -> editGroup());
        this.removeGroupButton.addActionListener(e -> removeGroup());
        this.addProfileButton.addActionListener(e -> addProfile());
        this.editProfileButton.addActionListener(e -> editProfile());
        this.removeProfileButton.addActionListener(e -> removeProfile());
        log.logInfo("Settings UI initialized.");
        return this.panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        final var language = Language.getLocaleFromDisplay((String) languageCombo.getSelectedItem());
        currentSettings.setLanguageName(language.getDisplayName());
        currentSettings.setLoggingEnabled(loggingCheckBox.isSelected());
        currentSettings.setGroups(groupConfigTableModel.getItems());
        currentSettings.setActiveProfile(
                currentSettings.getProfiles().stream()
                        .filter(p -> p.getName().equals(profilesCombo.getSelectedItem()))
                        .findFirst()
                        .orElse(null)
        );
        final var settings = TestManagerSettings.getInstance();

        XmlSerializerUtil.copyBean(currentSettings, settings);
        log.logInfo("Settings applied: " + currentSettings);
    }

    @Override
    public void reset() {
        final var settings = TestManagerSettings.getInstance();

        currentSettings = new TestManagerSettings();

        XmlSerializerUtil.copyBean(settings, currentSettings);

        languageCombo.setSelectedItem(currentSettings.getLanguageName());
        loggingCheckBox.setSelected(currentSettings.isLoggingEnabled());

        updateTableFromSettings();
        updateProfilesComboFromSettings();

        log.logInfo("Settings reset to: " + currentSettings);
    }

    private void addGroup() {
        final var dialog = new GroupEdit(null);
        if (dialog.showAndGet()) {
            final var newGroup = dialog.getGroup();
            currentSettings.addGroup(newGroup);
            groupConfigTableModel.addRow(newGroup);
            log.logInfo("New group added: " + newGroup);
        }
    }

    private void editGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            log.logInfo("Edit group attempted with no selection.");
            return;
        }
        final var existing = currentSettings.getGroups().get(selectedRow);
        final var dialog = new GroupEdit(existing);
        if (dialog.showAndGet()) {
            final var updated = dialog.getGroup();
            currentSettings.updateGroup(selectedRow, updated);
            groupConfigTableModel.updateRow(selectedRow, updated);
            log.logInfo("Group updated at row " + selectedRow + ": " + updated);
        }
    }

    private void removeGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            log.logInfo("Remove group attempted with no selection.");
            return;
        }
        final var toRemove = currentSettings.getGroups().get(selectedRow);
        currentSettings.removeGroup(toRemove);
        groupConfigTableModel.removeRow(selectedRow);
        log.logInfo("Group removed at row " + selectedRow + ": " + toRemove);
    }

    private void updateTableFromSettings() {
        groupConfigTableModel = new GroupConfigTableModel(new ArrayList<>(currentSettings.getGroups()));
        groupsTable.setModel(groupConfigTableModel);
    }

    private void addProfile() {
        final var dialog = new ProfileEdit(null);
        if (dialog.showAndGet()) {
            final var newProfile = dialog.getProfileConfigData();
            currentSettings.addProfile(newProfile);
            updateProfilesComboFromSettings();
            log.logInfo("New profile added: " + newProfile);
        }
    }

    private void editProfile() {
        final var selectedName = (String) profilesCombo.getSelectedItem();
        if (selectedName == null) {
            log.logInfo("Edit profile attempted with no selection.");
            return;
        }
        final var existing = currentSettings.getProfiles().stream()
                .filter(p -> p.getName().equals(selectedName))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            log.logInfo("Edit profile attempted but profile not found: " + selectedName);
            return;
        }
        final var dialog = new ProfileEdit(existing);
        if (dialog.showAndGet()) {
            final var updated = dialog.getProfileConfigData();
            currentSettings.updateProfile(existing, updated);
            updateProfilesComboFromSettings();
            log.logInfo("Profile updated: " + updated);
        }
    }

    private void removeProfile() {
        final var selectedName = (String) profilesCombo.getSelectedItem();
        if (selectedName == null) {
            log.logInfo("Remove profile attempted with no selection.");
            return;
        }
        final var toRemove = currentSettings.getProfiles()
                .stream()
                .filter(p -> p.getName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (toRemove == null) {
            log.logInfo("Remove profile attempted but profile not found: " + selectedName);
            return;
        }
        currentSettings.removeProfile(toRemove);
        if (currentSettings.getActiveProfile() != null && currentSettings.getActiveProfile().equals(toRemove)) {
            log.logInfo("Active profile removed, clearing active profile.");
            currentSettings.setActiveProfile(null);
        }
        updateProfilesComboFromSettings();
        log.logInfo("Profile removed: " + toRemove);
    }

    private void updateProfilesComboFromSettings() {
        profilesCombo.removeAllItems();
        for (var profile : currentSettings.getProfiles()) {
            profilesCombo.addItem(profile.getName());
        }
        if (currentSettings.getActiveProfile() != null) {
            profilesCombo.setSelectedItem(currentSettings.getActiveProfile().getName());
        } else {
            profilesCombo.setSelectedIndex(-1);
        }
        updateGroupUI();
    }

    private void updateGroupUI() {
        groupConfigTableModel.fireTableRowsUpdated(0, groupConfigTableModel.getRowCount() - 1);
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
        panel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                new Dimension(258, 40), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel2.putClientProperty("html.disable", Boolean.FALSE);
        panel1.add(panel2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        languageLabel = new JLabel();
        languageLabel.setText("Language");
        panel2.add(languageLabel);
        languageCombo = new JComboBox();
        panel2.add(languageCombo);
        loggingCheckBox = new JCheckBox();
        loggingCheckBox.setText("CheckBox");
        panel2.add(loggingCheckBox);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        activeProfileLabel = new JLabel();
        activeProfileLabel.setText("Active Profile:");
        panel3.add(activeProfileLabel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        profilesCombo = new JComboBox();
        panel3.add(profilesCombo,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        addProfileButton = new JButton();
        addProfileButton.setText("Add");
        panel3.add(addProfileButton,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        editProfileButton = new JButton();
        editProfileButton.setText("Edit");
        panel3.add(editProfileButton,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        removeProfileButton = new JButton();
        removeProfileButton.setText("Remove");
        panel3.add(removeProfileButton,
                new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        groupsTable = new JTable();
        scrollPane1.setViewportView(groupsTable);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        addGroupButton = new JButton();
        addGroupButton.setText("Add");
        panel5.add(addGroupButton,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        editGroupButton = new JButton();
        editGroupButton.setText("Edit");
        panel5.add(editGroupButton,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        removeGroupButton = new JButton();
        removeGroupButton.setText("Remove");
        panel5.add(removeGroupButton,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return panel;}
}
