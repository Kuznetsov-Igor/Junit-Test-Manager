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
import org.jetbrains.annotations.Nullable;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Действие для генерации классов-генераторов тестовых данных для выбранного класса.
 */
public class GenerateTestDataGeneratorsAction extends AnAction {
    private static final LoggerUtils log = LoggerUtils.getLogger(GenerateTestDataGeneratorsAction.class);

    /**
     * Выполняет генерацию классов-генераторов тестовых данных для класса из контекста.
     *
     * @param e событие действия
     */
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

    /**
     * Обновляет видимость действия в зависимости от наличия редактора.
     *
     * @param e событие действия
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        final var editor = e.getData(CommonDataKeys.EDITOR);
        boolean isVisible = (editor != null);

        e.getPresentation().setVisible(isVisible);
        if (!isVisible) {
            log.logInfo("Action hidden: No editor found.");
        }
    }

    /**
     * Извлекает PsiClass из контекста действия.
     * Пытается найти класс в следующем порядке:
     * 1. PSI_ELEMENT (если выбран класс или файл)
     * 2. Файл из редактора (PSI_FILE)
     * 3. Файл из Project View (PSI_FILE)
     *
     * @param e событие действия
     * @return найденный класс или null
     */
    @Nullable
    private PsiClass getPsiClassFromContext(@NotNull AnActionEvent e) {
        // Сначала пробуем PSI_ELEMENT
        final var psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiClass psiClass) {
            return psiClass;
        }

        // Если PSI_ELEMENT — файл, берём первый класс
        if (psiElement instanceof PsiJavaFile javaFile) {
            return getFirstClassFromFile(javaFile);
        }

        // Если нет PSI_ELEMENT, берём из текущего файла редактора
        final var editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            final var file = e.getData(CommonDataKeys.PSI_FILE);
            if (file instanceof PsiJavaFile javaFile) {
                return getFirstClassFromFile(javaFile);
            }
        }

        // Если ничего, берём из PSI_FILE (если выбран файл в Project View)
        final var psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile instanceof PsiJavaFile javaFile) {
            return getFirstClassFromFile(javaFile);
        }

        return null;
    }

    /**
     * Извлекает первый класс из Java файла.
     *
     * @param javaFile Java файл
     * @return первый класс или null, если файл пустой
     */
    @Nullable
    private PsiClass getFirstClassFromFile(@NotNull PsiJavaFile javaFile) {
        final var classes = javaFile.getClasses();
        return classes.length > 0 ? classes[0] : null;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
