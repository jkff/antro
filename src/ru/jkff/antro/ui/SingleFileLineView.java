/*
 *
 *     This file is part of antro, the line-level profiler for ant build scripts.
 *
 *     antro is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     antro is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with antro.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.jkff.antro.ui;

import org.jdesktop.swingx.JXTable;
import ru.jkff.antro.AnnotatedFile;
import ru.jkff.antro.Stat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;

/**
 * Created on 13:05:38 22.03.2008
 *
 * @author jkff
 */
public class SingleFileLineView extends JScrollPane {
    private JXTable table;
    private AnnotatedFile file;

    public SingleFileLineView(AnnotatedFile f) {
        this.file = f;
        table = new JXTable(new DefaultTableModel(f.getLineCount(), 6));
        table.getColumn(0).setMaxWidth(45);
        table.getColumn(1).setMaxWidth(45);
        table.getColumn(2).setMaxWidth(45);
        table.getColumn(3).setMaxWidth(45);
        table.getColumn(4).setMaxWidth(45);
        table.getColumn(0).setHeaderValue("min");
        table.getColumn(1).setHeaderValue("max");
        table.getColumn(2).setHeaderValue("first");
        table.getColumn(3).setHeaderValue("total");
        table.getColumn(4).setHeaderValue("count");
        table.getColumn(5).setHeaderValue("text");
        table.setColumnSequence(new Object[]{"min", "max", "first", "total", "count", "text"});

        for (int i = 0; i < f.getLineCount(); ++i) {
            setValues(table, i, f.getLine(i), f.getStat(i));
        }

        table.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private JLabel label = new JLabel();

            {
                label.setFont(new Font("Courier New", Font.PLAIN, 11));
                label.setOpaque(true);
            }

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());
                if (isSelected) {
                    label.setBackground(Color.BLUE);
                    label.setForeground(Color.WHITE);
                } else {
                    if (row % 2 == 0) {
                        label.setBackground(new Color(249, 252, 242));
                    } else {
                        label.setBackground(new Color(255, 255, 255));
                    }
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });

        getViewport().add(table);

        table.addComponentListener(new ComponentAdapter() {
            private boolean done = false;

            public void componentShown(ComponentEvent e) {
                if (done) return;
                done = true;
                for (int i = 0; i < 5; ++i) {
                    table.getColumn(i).sizeWidthToFit();
                }
            }
        });
    }

    private void setValues(JXTable table, int i, String line, Stat stat) {
        if (stat != null) {
            table.setValueAt(toString(stat.min), i, 0);
            table.setValueAt(toString(stat.max), i, 1);
            table.setValueAt(toString(stat.first), i, 2);
            table.setValueAt(toString(stat.total), i, 3);
            table.setValueAt(stat.count, i, 4);
        }
        table.setValueAt(unescape(line), i, 5);
    }

    private static NumberFormat ONE_DECIMAL = NumberFormat.getNumberInstance();

    static {
        ONE_DECIMAL.setMaximumFractionDigits(1);
        ONE_DECIMAL.setMinimumFractionDigits(1);
    }

    private String toString(double val) {
        return ONE_DECIMAL.format(val);
    }

    private String unescape(String val) {
        return val.replace("&lt;", "<").replace("&gt;", ">");
    }

    public void jumpToLine(int line) {
        table.getSelectionModel().setSelectionInterval(line-1, line-1);
        table.scrollRowToVisible(line);
    }

    public String getFile() {
        return file.getName();
    }

    public int getSelectedLine() {
        return table.getSelectedRow() + 1;
    }
}
