package com.my.junit.testmanager.utils;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.my.junit.testmanager.config.data.GroupConfigData;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Утилитарный класс для создания JUnit тестовых конфигураций.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JunitTestConfigurationUtils {
    private static final LoggerUtils log = LoggerUtils.getLogger(JunitTestConfigurationUtils.class);

    /**
     * Создаёт JUnit тестовую конфигурацию для указанного списка классов и группы.
     *
     * @param project    Текущий проект.
     * @param psiClasses Список классов для конфигурации.
     * @param group      Данные группы для конфигурации.
     */
    public static void createJunitConfigurationTest(
            @NotNull Project project,
            @NotNull List<PsiClass> psiClasses,
            @NotNull GroupConfigData group
    ) {
        log.logInfo("Creating test configuration for group: " + group.getName());

        try {
            final var runManager = RunManager.getInstance(project);

            var configName = group.getName();
            if (psiClasses.size() > 1) {
                configName += " (Multiple Classes) size " + psiClasses.size();
            }

            final var type = ConfigurationTypeUtil.findConfigurationType("JUnit");
            if (type == null) {
                log.logInfo("JUnit configuration type not found");
                MessagesDialogUtils.messageWarn(
                        project,
                        message("dialog.junit.plugin.not.enabled")
                );
                return;
            }

            final var factories = type.getConfigurationFactories();
            final var settings = runManager.createConfiguration(
                    configName,
                    factories[0]
            );

            final var config = settings.getConfiguration();

            final var classLoader = config.getClass().getClassLoader();
            final var psiMethodClass = classLoader.loadClass("com.intellij.psi.PsiMethod");
            final var method = config.getClass()
                    .getMethod(
                            "bePatternConfiguration",
                            List.class,
                            psiMethodClass
                    );

            method.invoke(config, psiClasses, null);

            if (group.getVmArgs() != null && !group.getVmArgs().trim().isEmpty()) {
                final var setVmMethod = config.getClass().getMethod("setVMParameters", String.class);
                setVmMethod.invoke(config, group.getVmArgs());
                log.logInfo("Set VM args: " + group.getVmArgs());
            }

            settings.setName(configName);

            runManager.addConfiguration(settings);
            log.logInfo("Configuration created: " + configName);
        } catch (Exception e) {
            log.logError("Failed to create configuration: ", e);
            MessagesDialogUtils.messageError(
                    project,
                    message("dialog.test.configurations.creation.failed")
            );
        }
    }
}
