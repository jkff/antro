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

import ru.jkff.antro.OurLocation;
import ru.jkff.antro.Trace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Created on 17:33:03 17.03.2008
 *
 * @author jkff
 */
public class BarView extends JScrollPane {
    private BarControl bars;
    private TraceView traceView;

    // When c.percentOfParent is less than this, bars of its children are not painted
    private static final double EXPAND_THRESHOLD = 5;
    private static final double PAINT_THRESHOLD = 3;
    private static final double TOP_PAINT_THRESHOLD = 1;
    private static final Color HIGHLIGHTED_BAR_COLOR = Color.BLUE;

    public BarView() {
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void focusOn(Trace trace) {
        if (bars != null)
            remove(bars);
        bars = new BarControl();
        bars.focusOn(trace);
        getViewport().add(bars);
        repaint();
    }

    private void selectInTree(Trace t) {
        traceView.select(t);
    }

    private void highlightBars(OurLocation loc) {
        bars.highlightLocation(loc);
    }

    public void setTraceView(TraceView traceView) {
        this.traceView = traceView;
    }

    private class BarControl extends JPanel {
        private Trace focus;

        private OurLocation highlightedLocation;

        private List<Row> rows;

        private static final int BAR_HEIGHT = 20;

        public BarControl() {
            setToolTipText("");
            ToolTipManager.sharedInstance().registerComponent(this);
            this.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    rows = buildRows();
                    repaint();
                }
            });

            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Trace trace = getTraceAtPoint(e.getX(), e.getY());
                    if (trace == null)
                        return;

                    if(e.getClickCount() == 1) {
                        highlightBars(trace.getCall().location);
                    } else {
                        selectInTree(trace);
                    }
                }
            });
        }

        public void focusOn(Trace trace) {
            this.focus = trace;
            rows = buildRows();
            repaint();
        }

        private List<Row> buildRows() {
            List<Row> res = new ArrayList<Row>();
            fillRows(res, focus, 0, 0, getWidth(), true, focus.getTotalTime());
            return res;
        }

        private void fillRows(List<Row> res, Trace trace, int rowIndex, double x0, double width, boolean recurse, double total) {
            if (res.size() == rowIndex) {
                res.add(new Row());
            } else if (res.size() < rowIndex) {
                throw new AssertionError();
            }
            res.get(rowIndex).add((int) x0, (int) width, trace);

            if (!recurse)
                return;

            double cx0 = x0;
            for (Trace child : trace.getChildren()) {
                if (trace.getTotalTime() == 0)
                    continue;

                double dw = width * child.getTotalTime() / trace.getTotalTime();
                boolean recIntoChild = child.getPercentOfParent() > EXPAND_THRESHOLD;

                if (child.getPercentOfParent() > PAINT_THRESHOLD &&
                        100 * child.getTotalTime() / total > TOP_PAINT_THRESHOLD) {
                    fillRows(res, child, rowIndex + 1, cx0, dw, recIntoChild, total);
                }

                cx0 += dw;
            }
        }

        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                for (Row.Bar bar : row.bars) {
                    paintBar(g, bar, BAR_HEIGHT * i);
                }
            }
        }

        private void paintBar(Graphics g, Row.Bar bar, int y) {
            int x0 = bar.x0, width = bar.width;
            Trace trace = bar.t;

            g.setColor(Color.GRAY);
            g.drawLine(x0 + 1, y, x0 + width - 1, y);
            g.setColor(Color.BLACK);
            g.drawLine(x0, y, x0, y + BAR_HEIGHT - 1);
            g.setColor(Color.DARK_GRAY);
            g.drawLine(x0 + 1, y + BAR_HEIGHT - 1, x0 + width, y + BAR_HEIGHT - 1);
            g.drawLine(x0 + width, y, x0 + width, y + BAR_HEIGHT - 1);


            Color color;
            if (trace.getCall().location.equals(highlightedLocation)) {
                color = HIGHLIGHTED_BAR_COLOR;
            } else {
                color = toColor(getPercent(trace));
            }
            g.setColor(color);
            g.fillRect(x0, y, width, BAR_HEIGHT);

            String msg = describe(trace);

            g.setFont(new Font("Times New Roman", Font.PLAIN, 9));
            int sw = g.getFontMetrics().stringWidth(msg);
            if (sw >= width) {
                // Try to shorten the string
                msg = new Formatter().format("%.0fs %s", trace.getTotalTime(), trace.getCall().name).toString();
                sw = g.getFontMetrics().stringWidth(msg);
                if (sw >= width) {
                    // Try to shorten again
                    msg = trace.getCall().name;
                    sw = g.getFontMetrics().stringWidth(msg);
                    if (sw < width) {
                        msg = trace.getCall().name;
                    } else {
                        // Everything failed
                        return;
                    }
                }
            }
            int h = g.getFontMetrics().getHeight();
            g.setColor(Color.BLACK);
            g.drawString(msg, x0 + (width - sw) / 2, y + BAR_HEIGHT / 2 + h / 2);
        }

        private double getPercent(Trace trace) {
            if (focus.getTotalTime() == 0)
                return 0;
            else
                return 100 * trace.getTotalTime() / focus.getTotalTime();
        }

        private Color toColor(double percent) {
            if (percent < 50) {
                return ColorUtil.blend(Color.GREEN, Color.YELLOW, percent / 50);
            } else {
                return ColorUtil.blend(Color.YELLOW, Color.RED, (percent - 50) / 50);
            }
        }

        public Trace getTraceAtPoint(int x, int y) {
            int row = getRowIndex(y);
            return row < rows.size() ? rows.get(row).getTraceAt(x) : null;
        }

        private int getRowIndex(int y) {
            return y / BAR_HEIGHT;
        }

        public String getToolTipText(MouseEvent event) {
            Trace t = getTraceAtPoint(event.getX(), event.getY());
            if (t == null) return null;
            return describe(t);
        }

        private String describe(Trace trace) {
            double percent = getPercent(trace);
            return new Formatter().format("%.0f%% %.0fs %s",
                    percent,
                    trace.getTotalTime(),
                    trace.getCall().name).toString();
        }

        public void highlightLocation(OurLocation loc) {
            highlightedLocation = loc;
            repaint();
        }

        private class Row {
            private class Bar {
                int x0, width;
                Trace t;
            }

            ArrayList<Bar> bars = new ArrayList<Bar>();

            public void add(int x0, int width, Trace t) {
                Bar bar = new Bar();
                bar.x0 = x0;
                bar.width = width;
                bar.t = t;
                bars.add(bar);
            }

            public Trace getTraceAt(int x) {
                for (Bar bar : bars) {
                    if (bar.x0 < x && bar.x0 + bar.width > x)
                        return bar.t;
                }
                return null;
            }
        }
    }
}
