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

import ru.jkff.antro.Call;
import ru.jkff.antro.Report;
import ru.jkff.antro.Trace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created on 17:46:48 17.03.2008
 *
 * @author jkff
 */
public class TraceCellRenderer implements TreeCellRenderer {
    private JLabel render;

    private Mode mode;
    private double total;
    private OurIcon icon;
    private NodeHighlighter highlighter;

    public TraceCellRenderer(Mode mode, Report report, NodeHighlighter highlighter) {
        this.mode = mode;
        if(mode == Mode.PERCENT_PARENT)
            this.total = report.getTrace().getTotalTime();
        this.highlighter = highlighter;
    }

    {
        render = new JLabel();
        render.setOpaque(true);
        icon = new OurIcon();
        render.setIcon(icon);
    }

    public Component getTreeCellRendererComponent(
            JTree tree, Object nodeObj, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObj;
        Trace trace = (Trace) node.getUserObject();
        double time;
        switch (mode) {
            case TOTAL:
                icon.relativeValue = trace.getTotalTime()/total;
                time = trace.getTotalTime();
                break;
            case OWN:
                icon.relativeValue = trace.getPercentOfParent()/100;
                time = trace.getOwnTime();
                break;
            case PERCENT_PARENT:
                icon.relativeValue = trace.getPercentOfParent()/100;
                time = trace.getTotalTime();
                break;
            case PERCENT_TOTAL:
                icon.relativeValue = trace.getTotalTime()/total;
                time = trace.getTotalTime();
                break;
            default:
                throw new AssertionError();
        }
        Call call = trace.getCall();
        String desc;
        switch (call.kind) {
            case BUILD:
                desc = "[" + call.name + "]";
                break;
            case SUBBUILD:
                desc = "->[" + call.name + "]";
                break;
            case TARGET:
                desc = "#" + call.name;
                break;
            case TASK:
                desc = "<" + call.name + ">";
                break;
            default:
                throw new AssertionError();
        }

        NodeStyle style = highlighter.getStyle(node);

        if(style.getTextAttributes().isBold())
            render.setFont(render.getFont().deriveFont(Font.BOLD));
        else
            render.setFont(render.getFont().deriveFont(Font.PLAIN));

        if(selected) {
            render.setBackground(Color.BLUE);
            render.setForeground(Color.WHITE);
        } else {
            render.setBackground(style.getBackgroundColor());
            render.setForeground(style.getForegroundColor());
        }

        render.setText("" + time + "s - " + desc);
        return render;
    }

    private class OurIcon implements Icon {
        private static final int WIDTH = 40, HEIGHT = 15;

        private double relativeValue;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, x+WIDTH,y+HEIGHT);
            g.setColor(morph(Color.GREEN, Color.RED, relativeValue));
            g.fillRect(x, y, (int) (x+relativeValue*WIDTH),y+HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, x+WIDTH,y+HEIGHT);
        }

        private Color morph(Color a, Color b, double val) {
            return new Color(
                    (int)(b.getRed()*val + a.getRed()*(1-val)),
                    (int)(b.getGreen()*val + a.getGreen()*(1-val)),
                    (int)(b.getBlue()*val + a.getBlue()*(1-val)));
        }

        public int getIconWidth() {
            return WIDTH+1;
        }

        public int getIconHeight() {
            return HEIGHT+1;
        }
    }
}
