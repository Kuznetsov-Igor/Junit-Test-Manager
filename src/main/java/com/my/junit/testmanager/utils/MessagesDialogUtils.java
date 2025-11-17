package com.my.junit.testmanager.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Утилитарный класс для отображения диалоговых окон с сообщениями.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MessagesDialogUtils {

    public static void messageError(
            @Nullable Project project,
            @NotNull String message
    ) {
        Messages.showMessageDialog(
                project,
                message,
                message("dialog.title.error"),
                Messages.getErrorIcon()
        );
    }

    public static void messageInfo(
            @Nullable Project project,
            @NotNull String message
    ) {
        Messages.showMessageDialog(
                project,
                message,
                message("dialog.title.information"),
                Messages.getInformationIcon()
        );
    }

    public static void messageWarn(
            @Nullable Project project,
            @NotNull String message
    ) {
        Messages.showMessageDialog(
                project,
                message,
                message("dialog.title.warning"),
                Messages.getWarningIcon()
        );
    }
}
