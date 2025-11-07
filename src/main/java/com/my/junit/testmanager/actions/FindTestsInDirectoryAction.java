package com.my.junit.testmanager.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.my.junit.testmanager.data.SearchType;
import com.my.junit.testmanager.model.TestClassInfoTableModel;
import com.my.junit.testmanager.services.TestClassesFinder;
import com.my.junit.testmanager.ui.TestConfigurationsForm;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;

/**
 * Действие для поиска тестов в выбранной директории.
 */
public class FindTestsInDirectoryAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        final var project = e.getProject();
        final var file = e.getData(VIRTUAL_FILE);
        e.getPresentation().setEnabled(project != null && file != null && file.isDirectory());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final var project = e.getProject();
        final var directory = e.getData(VIRTUAL_FILE);
        if (project != null && directory != null && directory.isDirectory()) {

            final var testClassCollector = new TestClassesFinder(project);
            final var directoryTests = testClassCollector.collect(
                    SearchType.DIRECTORY,
                    directory
            );

            new TestConfigurationsForm(
                    new TestClassInfoTableModel(directoryTests),
                    project
            ).showAndGet();
        }
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}


