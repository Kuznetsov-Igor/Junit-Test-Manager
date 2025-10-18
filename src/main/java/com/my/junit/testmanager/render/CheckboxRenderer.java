package com.my.junit.testmanager.render;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Рендерер для отображения чекбоксов в таблице.
 */
public class CheckboxRenderer extends DefaultTableCellRenderer {
    private final JCheckBox checkBox;

    public CheckboxRenderer() {
        checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        checkBox.setSelected(value != null && (Boolean) value);

        if (table.isCellEditable(row, column)) {
            checkBox.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            checkBox.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        }
        return checkBox;
    }
}

