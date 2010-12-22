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

import ru.jkff.antro.AnnotatedFile;
import ru.jkff.antro.OurLocation;
import ru.jkff.antro.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 17:33:00 17.03.2008
 *
 * @author jkff
 */
public class LineView extends JPanel {
    private Report report;
    private JTabbedPane tabs;
    private Map<String, Integer> tabIndices = new HashMap<String, Integer>();
    private TraceView traceView;

    public LineView() {
    }

    public void jumpToLine(String file, int line) {
        Integer tabIndex = tabIndices.get(file);
        if(tabIndex == null)
            return;
        SingleFileLineView v = (SingleFileLineView) tabs.getComponentAt(tabIndex);

        tabs.setSelectedIndex(tabIndex);
        v.jumpToLine(line);
    }

    public void load(Report report) {
        unload();

        this.report = report;
        
        tabs = new JTabbedPane();
        int i = 0;
        for (String file : report.getUsedBuildFiles()) {
            tabIndices.put(file, i++);
            tabs.addTab(new File(file).getName(), createLineViewForFile(file));
        }

        this.setLayout(new BorderLayout());
        this.add(tabs, BorderLayout.CENTER);


        this.registerKeyboardAction(new HighlightUsagesAction(), "Highlight usages",
                KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void unload() {
        removeAll();
        this.report = null;
        this.tabs = null;
        this.tabIndices.clear();
    }

    private Component createLineViewForFile(String file) {
        AnnotatedFile f = report.getAnnotatedFile(file);
        return new SingleFileLineView(f);
    }

    public void setTraceView(TraceView traceView) {
        this.traceView = traceView;
    }

    private class HighlightUsagesAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            highlightSelectedLineUsages();
        }
    }

    private void highlightSelectedLineUsages() {
        if(tabs.getSelectedComponent() == null)
            return;
        SingleFileLineView v = (SingleFileLineView) tabs.getSelectedComponent();

        String file = v.getFile();
        int line = v.getSelectedLine();
        if(line < 0)
            return;

        traceView.highlightLineUsages(new OurLocation(file, line));
    }
}
