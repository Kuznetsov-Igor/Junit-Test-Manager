package com.my.junit.testmanager.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.my.junit.testmanager.data.SearchType;
import com.my.junit.testmanager.model.TestClassRelocationTableModel;
import com.my.junit.testmanager.services.RelocationAnalyzer;
import com.my.junit.testmanager.ui.TableRelocationForm;
import com.my.junit.testmanager.utils.MessagesDialogUtils;
import org.jetbrains.annotations.NotNull;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Действие для релокации тестов, связанных с изменениями в последних коммитах.
 */
public class RelocateTestsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final var project = e.getProject();
        if (project == null) {
            return;
        }
        final var analyzer = new RelocationAnalyzer(project);

        final var items = analyzer.analyze(SearchType.ALL);
        if (items.isEmpty()) {
            MessagesDialogUtils.messageWarn(
                    project,
                    message("dialog.relocate.tests.no.tests.found")
            );
            return;
        }
        new TableRelocationForm(
                new TestClassRelocationTableModel(items),
                project
        ).showAndGet();
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}









