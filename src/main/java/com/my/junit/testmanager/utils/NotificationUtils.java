package com.my.junit.testmanager.utils;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NotificationUtils {
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("JUnitTestManagerNotifications");

    /**
     * Показать информационное уведомление
     *
     * @param message текст сообщения
     */
    public static void showInfoNotification(@NotNull String message) {
        NOTIFICATION_GROUP.createNotification(message, NotificationType.INFORMATION);
    }

    /**
     * Показать предупреждающее уведомление
     *
     * @param message текст сообщения
     */
    public static void showWarningNotification(@NotNull String message) {
        NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING);
    }

    /**
     * Показать уведомление об ошибке
     *
     * @param message текст сообщения
     */
    public static void showErrorNotification(@NotNull String message) {
        NOTIFICATION_GROUP.createNotification(message, NotificationType.ERROR);
    }
}
