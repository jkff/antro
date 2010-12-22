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

import ru.jkff.antro.Report;
import ru.jkff.antro.ReportReader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Properties;

/**
 * Created on 14:29:30 17.03.2008
 *
 * @author jkff
 */
public class UI extends JFrame {
    private BarView barView;
    private TraceView traceView;
    private LineView lineView;

    public UI() {
        super("Ant profiler");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        traceView = new TraceView();
        lineView = new LineView();
        barView = new BarView();

        traceView.setLineView(lineView);
        traceView.setBarView(barView);
        lineView.setTraceView(traceView);
        barView.setTraceView(traceView);

        JSplitPane vertical = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        JSplitPane leftHorizontal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
        getContentPane().add(vertical);

        leftHorizontal.setTopComponent(traceView);
        leftHorizontal.setBottomComponent(barView);

        vertical.setLeftComponent(leftHorizontal);
        vertical.setRightComponent(lineView);

        createMenu();

        setPreferredSize(new Dimension(1024, 768));
        pack();
        setVisible(true);
        vertical.setDividerLocation(0.5);
        leftHorizontal.setDividerLocation(0.8);
        pack();

        UIUtils.center(this);

        if (!Boolean.valueOf(getConf().getProperty("notFirstTime"))) {
            JOptionPane.showMessageDialog(null,
                    "Welcome to antro.\n" +
                            "This window is shown because you launched antro for the first time. Later it will be available through Help -> Keys.\n\n" +
                            "F4 in tree - show source\n" +
                            "Ctrl-Shift-F7 in tree or source - highlight usages of that source line in tree and bar view\n" +
                            "F3/Shift-F3 - cycle between usages (not implemented yet)",
                    "About", JOptionPane.INFORMATION_MESSAGE);
            getConf().setProperty("notFirstTime", "true");
            writeConf();
    }
    }

    void load(Report report) {
        traceView.load(report);
        lineView.load(report);
        pack();
        repaint();
    }

    private void createMenu() {
        setJMenuBar(new JMenuBar());
        getJMenuBar().add(createFileMenu());
        getJMenuBar().add(createHelpMenu());

}

    private JMenu createFileMenu() {
        JMenu res = new JMenu("File");
        res.add(new JMenuItem(OPEN));
        return res;
    }

    private JMenu createHelpMenu() {
        JMenu res = new JMenu("Help");
        res.add(new JMenuItem(KEYS));
        res.add(new JMenuItem(ABOUT));
        return res;
    }

    private final Action OPEN = new AbstractAction("Open...") {
        public void actionPerformed(ActionEvent e) {
            String file = chooseFile();
            if (file != null) {
                try {
                    Report report = new ReportReader().readReport(file);

                    getConf().setProperty("mostRecentFile", file);
                    writeConf();

                    load(report);
                } catch (IOException x) {
                    JOptionPane.showMessageDialog(
                            UI.this, "Can't load report:\n" + getStackTrace(x),
                            "Can't load report", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };


    private final Action KEYS = new AbstractAction("Keys") {
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null,
                    "F4 in tree - show source\n" +
                            "Ctrl-Shift-F7 in tree or source - highlight usages of that source line in tree and bar view\n" +
                            "F3/Shift-F3 - cycle between usages (not implemented yet)",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    private final Action ABOUT = new AbstractAction("About") {
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null,
                    "antro - the line level profiler for ant build scripts.\n" +
                            "http://sourceforge.net/projects/antro\n" +
                            "Copyright (c) Eugene Kirpichov aka jkff 2008\n" +
                            "( ekirpichov@gmail.com )", "About", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    private JFileChooser fileChooser = new JFileChooser();

    private String chooseFile() {
        fileChooser.setCurrentDirectory(getCurrentDirectoryForFileChooser());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".json");
            }

            public String getDescription() {
                return "Reports (*.json)";
            }
        });

        int res = fileChooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) {
            return null;
        } else {
            currentDirectory = fileChooser.getCurrentDirectory();
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
    }


    private File currentDirectory;

    private File getCurrentDirectoryForFileChooser() {
        if (currentDirectory == null) {
            File recentlyOpenedReport = getMostRecentlyOpenedReport();
            if (recentlyOpenedReport != null)
                currentDirectory = recentlyOpenedReport.getParentFile();
            else
                currentDirectory = File.listRoots()[0];
        }

        return currentDirectory;
    }

    private File getMostRecentlyOpenedReport() {
        String res = getConf().getProperty("mostRecentFile");
        return res == null ? null : new File(res);
    }

    private Properties conf;

    private Properties getConf() {
        if (conf != null)
            return conf;

        File ourConf = getOurConfFile();
        if (ourConf == null)
            return new Properties();

        conf = new Properties();
        try {
            conf.load(new FileInputStream(ourConf));
        } catch (FileNotFoundException e) {
            writeConf(); // Write empty conf = create conf file
        } catch (IOException e) {
            conf = null;
        }

        return conf;
    }

    private File getOurConfFile() {
        File userDir = new File(System.getProperty("user.home"));
        File ourDir = new File(userDir, ".antro");
        if (!ourDir.isDirectory() && !ourDir.mkdirs())
            return null;

        return new File(ourDir, "antro.properties");
    }

    private void writeConf() {
        File confFile = getOurConfFile();
        if (confFile == null)
            return;

        try {
            FileOutputStream fos = new FileOutputStream(confFile);
            if (conf != null)
                conf.store(fos, "");
            fos.close();
        } catch (IOException e) {
            // Nothing
        }
    }

    private static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
