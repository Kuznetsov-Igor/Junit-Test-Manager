package com.my.junit.testmanager.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.my.junit.testmanager.config.TestManagerConfig;
import com.my.junit.testmanager.config.data.ProfileData;
import com.my.junit.testmanager.data.Language;
import com.my.junit.testmanager.model.GroupConfigTableModel;
import com.my.junit.testmanager.utils.LoggerUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Конфигурация настроек плагина в настройках IDE.
 */
public class SettingsConfigurationForm implements Configurable {
    private JPanel panel;
    private JLabel languageLabel;
    private JComboBox<String> languageCombo;
    private JCheckBox loggingCheckBox;
    private JLabel activeProfileLabel;
    private JComboBox<String> profilesComboBox;
    private JButton addProfileButton;
    private JButton editProfileButton;
    private JButton removeProfileButton;
    private JTable groupsTable;
    private JButton addGroupButton;
    private JButton editGroupButton;
    private JButton removeGroupButton;

    private GroupConfigTableModel groupConfigTableModel;
    private TestManagerConfig currentSettings = TestManagerConfig.getInstance();
    private final LoggerUtils log = LoggerUtils.getLogger(SettingsConfigurationForm.class);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return message("settings.title.test.manager");
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
        loadedSettingsData();

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
        final var settings = getSettings();
        return !this.currentSettings.isStateEquals(settings);
    }

    @Override
    public void apply() {
        final var settings = getSettings();
        XmlSerializerUtil.copyBean(settings, this.currentSettings);
        log.logInfo("Settings applied: " + this.currentSettings);
    }

    @Override
    public void reset() {
        loadedSettingsData();
        log.logInfo("Settings reset to: " + this.currentSettings);
    }

    private void addGroup() {
        final var dialog = new GroupForm(null);
        if (dialog.showAndGet()) {
            final var newGroup = dialog.getGroup();
            this.groupConfigTableModel.addRow(newGroup);
            log.logInfo("New group added: " + newGroup);
        }
    }

    private void editGroup() {
        int selectedRow = this.groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            log.logInfo("Edit group attempted with no selection.");
            return;
        }
        final var existing = this.groupConfigTableModel.getItems().get(selectedRow);
        final var dialog = new GroupForm(existing);
        if (dialog.showAndGet()) {
            final var updated = dialog.getGroup();
            this.groupConfigTableModel.updateRow(selectedRow, updated);
            log.logInfo("Group updated at row " + selectedRow + ": " + updated);
        }
    }

    private void removeGroup() {
        int selectedRow = this.groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            log.logInfo("Remove group attempted with no selection.");
            return;
        }
        final var toRemove = this.groupConfigTableModel.getItems().get(selectedRow);
        this.groupConfigTableModel.removeRow(selectedRow);
        log.logInfo("Group removed at row " + selectedRow + ": " + toRemove);
    }

    private void addProfile() {
        final var dialog = new ProfileForm(null);
        if (dialog.showAndGet()) {
            final var newProfile = dialog.getProfileConfigData();
            if (newProfile.getName().equals(ProfileData.DEFAULT.getName())) {
                return;
            }
            this.profilesComboBox.addItem(newProfile.getName());
            log.logInfo("New profile added: " + newProfile);
        }
    }

    private void editProfile() {
        final var selectedName = (String) this.profilesComboBox.getSelectedItem();
        if (selectedName == null || selectedName.equals(ProfileData.DEFAULT.getName())) {
            log.logInfo("Edit profile attempted with no selection.");
            return;
        }
        final var oldProfile = ProfileData.of(selectedName);
        final var dialog = new ProfileForm(oldProfile);
        if (dialog.showAndGet()) {
            final var updated = dialog.getProfileConfigData();

            this.profilesComboBox.removeItem(selectedName);
            this.profilesComboBox.addItem(updated.getName());
            updateProfileToUpdateGroups(oldProfile, updated);

            log.logInfo("Profile updated: " + updated);
        }
    }

    private void removeProfile() {
        final var selectedName = (String) this.profilesComboBox.getSelectedItem();
        if (selectedName == null || selectedName.equals(ProfileData.DEFAULT.getName())) {
            log.logInfo("Remove profile attempted with no selection.");
            return;
        }
        this.profilesComboBox.removeItem(selectedName);
        removeProfileToUpdateGroups(ProfileData.of(selectedName));
        log.logInfo("Profile removed: " + selectedName);
    }

    /**
     * Обновляет профиль во всех группах, которые его содержат.
     *
     * @param oldProfile старый профиль для замены
     * @param newProfile новый профиль
     */
    private void updateProfileToUpdateGroups(@NotNull ProfileData oldProfile, @NotNull ProfileData newProfile) {
        final var groupsWithProfile = this.groupConfigTableModel.getItems().stream()
                .filter(group -> group.getProfiles().stream()
                        .anyMatch(profile -> profile.getName().equals(oldProfile.getName())))
                .toList();

        groupsWithProfile.forEach(group -> {
            group.removeProfile(oldProfile);
            group.addProfile(newProfile);
        });

        if (!groupsWithProfile.isEmpty()) {
            this.groupConfigTableModel.fireTableDataChanged();
        }
    }

    /**
     * Удаляет профиль из всех групп, которые его содержат.
     *
     * @param oldProfile профиль для удаления
     */
    private void removeProfileToUpdateGroups(@NotNull ProfileData oldProfile) {
        final var groupsWithProfile = this.groupConfigTableModel.getItems().stream()
                .filter(group -> group.getProfiles().stream()
                        .anyMatch(profile -> profile.getName().equals(oldProfile.getName())))
                .toList();

        groupsWithProfile.forEach(group -> group.removeProfile(oldProfile));

        if (!groupsWithProfile.isEmpty()) {
            this.groupConfigTableModel.fireTableDataChanged();
        }
    }

    private void loadedSettingsData() {
        this.languageCombo.removeAllItems();

        Arrays.stream(Language.values()).forEach(lang ->
                this.languageCombo.addItem(lang.getDisplayName())
        );

        this.languageCombo.setSelectedItem(
                Language.getLocaleFromDisplay(this.currentSettings.getLanguageName()).getDisplayName()
        );

        this.profilesComboBox.removeAllItems();

        this.currentSettings.getProfiles().forEach(profile ->
                this.profilesComboBox.addItem(profile.getName())
        );
        this.profilesComboBox.setSelectedItem(currentSettings.getActiveProfile().getName());
        this.loggingCheckBox.setSelected(currentSettings.isLoggingEnabled());
        this.groupConfigTableModel = new GroupConfigTableModel(currentSettings.getGroups());
        this.groupsTable.setModel(groupConfigTableModel);

        log.logInfo("Settings data loaded into UI." + this.currentSettings);
    }

    @NotNull
    private TestManagerConfig getSettings() {
        final var settings = new TestManagerConfig();

        final var language = Language.getLocaleFromDisplay((String) this.languageCombo.getSelectedItem());
        final var selectedProfiles = (String) this.profilesComboBox.getSelectedItem();
        var profiles = IntStream.range(0, profilesComboBox.getItemCount())
                .mapToObj(profilesComboBox::getItemAt)
                .map(ProfileData::of)
                .toList();

        settings.setLanguageName(language.getDisplayName());
        settings.setLoggingEnabled(this.loggingCheckBox.isSelected());
        settings.setGroups(this.groupConfigTableModel.getItems());
        settings.setProfiles(profiles);
        settings.setActiveProfile(
                settings.getProfiles().stream()
                        .filter(p -> p.getName().equals(selectedProfiles))
                        .findFirst()
                        .orElse(null)
        );
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
        profilesComboBox = new JComboBox();
        panel3.add(profilesComboBox,
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
