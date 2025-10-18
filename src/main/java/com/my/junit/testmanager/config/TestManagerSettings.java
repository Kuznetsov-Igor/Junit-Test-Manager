package com.my.junit.testmanager.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.my.junit.testmanager.config.data.GroupConfigData;
import com.my.junit.testmanager.config.data.ProfileConfigData;
import com.my.junit.testmanager.data.Language;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@State(
        name = "JunitTestManagerSettings",
        storages = @Storage(
                value = "JunitTestManagerSettings.xml",
                roamingType = RoamingType.PER_OS
        )
)
@Data
public class TestManagerSettings implements PersistentStateComponent<TestManagerSettings> {
    /**
     * Язык интерфейса плагина (по умолчанию английский).
     */
    private String languageName = Language.DEFAULT.getDisplayName();

    /**
     * Включено ли логирование (по умолчанию true).
     */
    private boolean loggingEnabled = true;

    /**
     * Список групп
     */
    private List<GroupConfigData> groups = new ArrayList<>(List.of(GroupConfigData.DEFAULT));

    /**
     * Список профилей
     */
    private List<ProfileConfigData> profiles = new ArrayList<>(List.of(ProfileConfigData.DEFAULT));

    /**
     * Активный профиль
     */
    private ProfileConfigData activeProfile = ProfileConfigData.DEFAULT;

    /**
     * Получает единственный экземпляр настроек плагина.
     */
    public static TestManagerSettings getInstance() {
        return ApplicationManager.getApplication().getService(TestManagerSettings.class);
    }

    /**
     * Добавляет новую группу в список.
     */
    public void addGroup(@NotNull GroupConfigData group) {groups.add(group);}

    /**
     * Удаляет группу из списка.
     */
    public void removeGroup(@NotNull GroupConfigData group) {groups.remove(group);}

    /**
     * Обновляет группу по индексу.
     */
    public void updateGroup(int index, @NotNull GroupConfigData newGroup) {
        if (index >= 0 && index < groups.size()) {
            groups.set(index, newGroup);
        }
    }

    /**
     * Добавляет новый профиль в список.
     */
    public void addProfile(@NotNull ProfileConfigData profile) {profiles.add(profile);}

    /**
     * Удаляет профиль из списка.
     */
    public void removeProfile(@NotNull ProfileConfigData profile) {
        if (profile == ProfileConfigData.DEFAULT) {
            return;
        }
        profiles.remove(profile);
    }

    /**
     * Обновляет профиль во всех группах и в общем списке.
     *
     * @param oldProfile - профиль для замены (не null)
     * @param newProfile - новый профиль (не null)
     */
    public void updateProfile(
            @NotNull ProfileConfigData oldProfile,
            @NotNull ProfileConfigData newProfile
    ) {
        if (oldProfile == ProfileConfigData.DEFAULT) {
            return;
        }
        int index = profiles.indexOf(oldProfile);
        if (index != -1) {
            groups.stream()
                    .filter(group -> group.getProfiles().contains(oldProfile))
                    .forEach(group -> {
                        group.removeProfile(oldProfile);
                        group.addProfile(newProfile);
                    });
            profiles.set(index, newProfile);
        }
    }

    public Locale getLanguage() {
        return Language.getLocaleFromDisplay(languageName).getLocale();
    }

    @Nullable
    @Override
    public TestManagerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TestManagerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}


