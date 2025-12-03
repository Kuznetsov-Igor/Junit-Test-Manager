package com.my.junit.testmanager.ui.render;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Рендер ячеек для отображения статусов (цветовая дифференциация).
 */
public class StatusTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        final var component = super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
        );
        if (component instanceof JLabel label && value instanceof String status) {
            label.setText(status);
            if (!isSelected) {
                label.setForeground(getColorForStatus(status));
            }
        }
        return component;
    }

    private Color getColorForStatus(String status) {
        if (status == null) {
            return Color.GRAY;
        }
        return switch (status.toLowerCase()) {
            case "успех", "success" -> new Color(0x2E7D32);
            case "внимание", "warning", "пропущен", "ignored" -> new Color(0xF9A825);
            case "ошибка", "failed", "error" -> new Color(0xC62828);
            default -> Color.GRAY;
        };
    }
}

