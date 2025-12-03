package com.my.junit.testmanager.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Фабрика ToolWindow для панели результатов тестов.
 */
public class TestResultsToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String TOOL_WINDOW_ID = "JUnit Test Manager Results";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final var manager = project.getService(TestResultsToolWindowManager.class);
        if (manager != null) {
            manager.initToolWindow(toolWindow);
        }
    }
}

