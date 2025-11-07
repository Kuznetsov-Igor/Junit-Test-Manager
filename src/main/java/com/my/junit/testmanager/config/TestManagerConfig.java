package com.my.junit.testmanager.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.my.junit.testmanager.config.data.GroupData;
import com.my.junit.testmanager.config.data.ProfileData;
import com.my.junit.testmanager.data.Language;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@State(
        name = "JunitTestManagerSettings",
        storages = @Storage(
                value = "JunitTestManagerSettings.xml",
                roamingType = RoamingType.LOCAL
        )
)
@Data
@EqualsAndHashCode(callSuper = true)
public class TestManagerConfig extends AbstractPersistentStateComponent<TestManagerConfig> {

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
    private List<GroupData> groups = new ArrayList<>(List.of(GroupData.DEFAULT));

    /**
     * Список профилей
     */
    private List<ProfileData> profiles = new ArrayList<>(List.of(ProfileData.DEFAULT));

    /**
     * Активный профиль
     */
    private ProfileData activeProfile = ProfileData.DEFAULT;

    /**
     * Получает единственный экземпляр настроек плагина.
     */
    public static TestManagerConfig getInstance() {
        final var instance = ApplicationManager.getApplication()
                .getService(TestManagerConfig.class);
        if (instance == null) {
            return new TestManagerConfig();
        }
        return instance;
    }

    public Locale getLanguage() {
        return Language.getLocaleFromDisplay(languageName).getLocale();
    }

    @Override
    public boolean isStateEquals(@NotNull TestManagerConfig other) {
        return this.loggingEnabled == other.loggingEnabled
                && this.languageName.equals(other.languageName)
                && isListEquals(this.groups, other.groups)
                && isListEquals(this.profiles, other.profiles)
                && this.activeProfile.equals(other.activeProfile);
    }

    @Override
    @Nullable
    public TestManagerConfig getState() {
        return this;
    }
}


