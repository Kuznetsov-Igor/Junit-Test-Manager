package com.my.junit.testmanager.render;

import com.my.junit.testmanager.config.data.GroupConfigData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Рендерер для отображения групп в таблице.
 */
public class GroupTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
        );

        if (value instanceof GroupConfigData group) {
            var bg = group.getColor();
            setBackground(bg);
            setText(group.getName());
            setForeground(getContrastingColor(bg));
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }

    private Color getContrastingColor(@NotNull Color bg) {
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
}
