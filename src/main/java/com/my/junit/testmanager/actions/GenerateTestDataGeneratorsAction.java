package com.my.junit.testmanager.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.my.junit.testmanager.services.TestDataGenerator;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.MessagesDialogUtils;
import org.jetbrains.annotations.NotNull;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

public class GenerateTestDataGeneratorsAction extends AnAction {
    private static final LoggerUtils log = LoggerUtils.getLogger(GenerateTestDataGeneratorsAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final var project = e.getProject();
        if (project == null) {
            return;
        }

        final var clazz = getPsiClassFromContext(e);
        if (clazz == null) {
            MessagesDialogUtils.messageWarn(
                    project,
                    message("dialog.generate.test.data.generators.no.class.found")
            );
            return;
        }
        new TestDataGenerator(project)
                .generateGeneratorForClass(clazz);
    }

    @Override
    public void update(AnActionEvent e) {
        final var editor = e.getData(CommonDataKeys.EDITOR);
        boolean isVisible = (editor != null);

        e.getPresentation().setVisible(isVisible);
        if (!isVisible) {
            log.logInfo("Action hidden: No editor found.");
        }
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        // Сначала пробуем PSI_ELEMENT
        final var psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiClass psiClass) {
            return psiClass;
        }

        // Если PSI_ELEMENT — файл, берём первый класс
        if (psiElement instanceof PsiJavaFile javaFile) {
            final var classes = javaFile.getClasses();
            if (classes.length > 0) {
                return classes[0];
            }
        }

        // Если нет PSI_ELEMENT, берём из текущего файла редактора
        final var editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            final var file = e.getData(CommonDataKeys.PSI_FILE);
            if (file instanceof PsiJavaFile javaFile) {
                final var classes = javaFile.getClasses();
                if (classes.length > 0) {
                    return classes[0];
                }
            }
        }

        // Если ничего, берём из PSI_FILE (если выбран файл в Project View)
        final var psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile instanceof PsiJavaFile javaFile) {
            final var classes = javaFile.getClasses();
            if (classes.length > 0) {
                return classes[0];
            }
        }

        return null;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
