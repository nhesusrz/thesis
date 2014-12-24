/**
 * Copyright (C) 2014 Martín Pacheco.
 *
 * This file is part of my Thesis aplication called "Extraction and Analysis
 * System of Topics for Software History Reports". Faculty of Exact Sciences of
 * the UNICEN University. Tandil, Argentine. http://www.exa.unicen.edu.ar/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: mpacheco@alumnos.exa.unicen.edu.ar
 *
 * @author Martín Pacheco
 */
package util;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class TextAreaRenderer extends JTextArea implements TableCellRenderer {

    private int iAtEndValue;
    private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private final JScrollPane scrollPane;

    public TextAreaRenderer(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        setLineWrap(true);
        setWrapStyleWord(true);
        setBorder(NO_FOCUS_BORDER);
    }

    /**
     * Returns a component for show correctly the DTO component in tables.
     *
     * @param obj
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return Component for the table cell.
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object obj, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setForeground(table.getForeground());
            super.setBackground(table.getBackground());
        }
        setFont(table.getFont());
        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            setBorder(NO_FOCUS_BORDER);
        }
        if (obj instanceof Integer) {
            setText(((Integer) obj).toString());
        } else {
            setText((String) obj);
        }
        // Adjust the height of the row
        final int currentHeight = table.getRowHeight(row);
        setSize(table.getColumnModel().getColumn(column).getWidth(), currentHeight);
        final int heightNeeded = (int) getPreferredSize().getHeight();
        if (heightNeeded > currentHeight) {
            table.setRowHeight(row, heightNeeded);
        }
        /**
         * If the scrollbar is positioned at the bottom of the viewport, keep it
         * positioned at the bottom, even if the viewport height changes.
         */
        final JScrollBar oScrollBar = scrollPane.getVerticalScrollBar();
        if (oScrollBar.getValue() == iAtEndValue) {
            oScrollBar.setValue(oScrollBar.getMaximum());
        }
        iAtEndValue = oScrollBar.getMaximum() - oScrollBar.getVisibleAmount();
        return this;
    }
}
