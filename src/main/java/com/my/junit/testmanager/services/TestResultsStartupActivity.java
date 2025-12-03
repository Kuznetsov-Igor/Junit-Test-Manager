package com.my.junit.testmanager.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Гарантирует инициализацию сервисов результатов тестов при открытии проекта.
 */
public class TestResultsStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        project.getService(TestResultsAggregatorService.class);
        project.getService(TestRunEventsListenerService.class);
    }
}

