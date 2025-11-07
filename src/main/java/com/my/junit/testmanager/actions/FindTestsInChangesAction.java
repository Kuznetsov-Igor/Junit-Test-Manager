package com.my.junit.testmanager.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.my.junit.testmanager.data.SearchType;
import com.my.junit.testmanager.model.TestClassInfoTableModel;
import com.my.junit.testmanager.services.TestClassesFinder;
import com.my.junit.testmanager.ui.TestConfigurationsForm;
import org.jetbrains.annotations.NotNull;

/**
 * Действие для поиска и отображения тестовых классов, измененных в последних коммитах.
 */
public class FindTestsInChangesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final var project = e.getProject();
        if (project == null) {
            return;
        }
        final var testClassCollector = new TestClassesFinder(project);
        final var diffTests = testClassCollector.collect(
                SearchType.CHANGES,
                null
        );

        new TestConfigurationsForm(
                new TestClassInfoTableModel(diffTests),
                project
        ).showAndGet();
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

