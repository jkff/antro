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

import org.jdesktop.swingx.JXTree;
import ru.jkff.antro.OurLocation;
import ru.jkff.antro.Report;
import ru.jkff.antro.Trace;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.IdentityHashMap;

/**
 * Created on 17:32:49 17.03.2008
 *
 * @author jkff
 */
public class TraceView extends JPanel {
    private Report report;

    private JXTree tree;
    private LineView lineView;

    private IdentityHashMap<Trace, DefaultMutableTreeNode> nodes;

    private static final Color HIGHLIGHTED_FG_COLOR = new Color(51, 0, 102);
    private static final Color HIGHLIGHTED_BG_COLOR = new Color(204, 153, 255);
    private static final Color GRAYED_FG_COLOR = Color.GRAY;

    private Mode mode;
    private BarView barView;

    public TraceView() {
        
    }

    public void highlightLineUsages(OurLocation location) {
        highlightUsages(TracePredicates.byLocation(location));
    }

    public void highlightUsages(Predicate<Trace> whichNodesToSelect) {
        setPredicateHighlighter(whichNodesToSelect);
        expandNodesAccordingToPredicate(whichNodesToSelect);
        repaint();
    }

    private void expandNodesAccordingToPredicate(Predicate<Trace> whichNodesToSelect) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        expandRecursively(node, whichNodesToSelect);
    }

    private void expandRecursively(
            DefaultMutableTreeNode node, Predicate<Trace> expandOrNot)
    {
        Trace trace = (Trace) node.getUserObject();

        if(expandOrNot.fits(trace)) {
            tree.expandPath(new TreePath(node.getPath()).getParentPath());
        } // No else branch! Don't collapse nodes that are already expanded

        Enumeration children = node.children();

        while(children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            expandRecursively(child, expandOrNot);
        }
    }

    private void setPredicateHighlighter(Predicate<Trace> whichNodesToSelect) {
        tree.setCellRenderer(createHighlightingCellRenderer(
                createPredicateHighlighter(whichNodesToSelect)));
    }

    public void load(Report report) {
        unload();

        this.report = report;

        this.nodes = new IdentityHashMap<Trace, DefaultMutableTreeNode>();

        tree = new JXTree(toNode(report.getTrace()));

        mode = Mode.PERCENT_PARENT;

        tree.setCellRenderer(createDefaultCellRenderer());

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if(path == null)
                    return;
                Trace trace = (Trace) (((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
                barView.focusOn(trace);
            }
        });

        JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().add(tree);

        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);

        this.registerKeyboardAction(
                new JumpToSourceLineAction(), "Jump to source",
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.registerKeyboardAction(
                new FindUsagesOfSameLineAction(), "Find usages of same line",
                KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.registerKeyboardAction(
                        new ToDefaultModeAction(), "To default mode",
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private Trace getTrace(TreeNode node) {
        return (Trace) ((DefaultMutableTreeNode)node).getUserObject();
    }

    private TreeCellRenderer createDefaultCellRenderer() {
        return new TraceCellRenderer(mode, report, new NodeHighlighter() {
            public NodeStyle getStyle(TreeNode node) {
                return new NodeStyle(Color.BLACK, Color.WHITE, TextAttributes.make());
            }
        });
    }

    private NodeHighlighter createPredicateHighlighter(final Predicate<Trace> predicate) {
        return new NodeHighlighter() {
            public NodeStyle getStyle(TreeNode node) {
                Trace trace = getTrace(node);

                if(predicate.fits(trace)) {
                    return new NodeStyle(HIGHLIGHTED_FG_COLOR, HIGHLIGHTED_BG_COLOR, TextAttributes.make().bold());
                } else {
                    return new NodeStyle(GRAYED_FG_COLOR, Color.WHITE, TextAttributes.make());
                }
            }
        };
    }

    private TreeCellRenderer createHighlightingCellRenderer(NodeHighlighter highlighter) {
        return new TraceCellRenderer(mode, report, highlighter);
    }


    private OurLocation getSelectedLocation() {
        if(tree.getSelectionCount() == 0)
            return null;
        TreePath selPath = tree.getSelectionPaths()[0];
        Trace trace = (Trace) ((DefaultMutableTreeNode)selPath.getLastPathComponent()).getUserObject();
        return trace.getCall().location;
    }

    public void jumpToSourceLine() {
        OurLocation loc = getSelectedLocation();
        if(loc == null)
            return;

        if(lineView != null) {
            lineView.jumpToLine(loc.fileName, loc.line);
        }
    }

    public void findUsagesOfSameLine() {
        highlightLineUsages(getSelectedLocation());
    }

    public void toDefaultMode() {
        tree.setCellRenderer(createDefaultCellRenderer());
        repaint();
    }

    public void setLineView(LineView lineView) {
        this.lineView = lineView;
    }

    public void setBarView(BarView barView) {
        this.barView = barView;
    }

    public void select(Trace t) {
        DefaultMutableTreeNode node = nodes.get(t);

        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path.getParentPath());
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }

    private class JumpToSourceLineAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            jumpToSourceLine();
        }
    }
    private class FindUsagesOfSameLineAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            findUsagesOfSameLine();
        }
    }
    private class ToDefaultModeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            toDefaultMode();
        }
    }

    private MutableTreeNode toNode(Trace trace) {
        DefaultMutableTreeNode res = new DefaultMutableTreeNode();
        res.setUserObject(trace);
        for (Trace child : trace.getChildren()) {
            res.add(toNode(child));
        }

        nodes.put(trace, res);

        return res;
    }

    private void unload() {
        this.removeAll();
        this.report = null;
    }
}
