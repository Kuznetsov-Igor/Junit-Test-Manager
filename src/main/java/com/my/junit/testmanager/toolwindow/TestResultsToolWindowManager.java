package com.my.junit.testmanager.toolwindow;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import com.my.junit.testmanager.data.TestResultRowData;
import com.my.junit.testmanager.data.TestResultsSummary;
import com.my.junit.testmanager.ui.TestResultsToolWindowForm;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

/**
 * Управляет жизненным циклом кастомного окна результатов тестов.
 */
@Service(Service.Level.PROJECT)
public final class TestResultsToolWindowManager {

    private final Project project;
    private TestResultsToolWindowForm form;

    public TestResultsToolWindowManager(@NotNull Project project) {
        this.project = project;
    }

    public synchronized void initToolWindow(@NotNull ToolWindow toolWindow) {
        if (form == null) {
            form = new TestResultsToolWindowForm(project);
        }
        final var contentFactory = ContentFactory.getInstance();
        final var content = contentFactory.createContent(form.getPanel(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    public void showResults(
            @NotNull TestResultsSummary summary,
            @NotNull List<TestResultRowData> rows
    ) {

        getApplication().invokeLater(() -> {
                    final var toolWindow = ToolWindowManager.getInstance(project)
                            .getToolWindow(TestResultsToolWindowFactory.TOOL_WINDOW_ID);
                    if (toolWindow == null) {
                        return;
                    }
                    if (form == null) {
                        initToolWindow(toolWindow);
                    }
                    if (!toolWindow.isVisible()) {
                        toolWindow.activate(null, false);
                    }
                    form.setResults(rows);
                    form.updateSummary(summary);
                },
                ModalityState.NON_MODAL,
                project.getDisposed()
        );
    }
}

