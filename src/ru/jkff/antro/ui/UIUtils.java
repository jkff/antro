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

import javax.swing.*;
import java.awt.*;

/**
 * Created on 10:52:39 25.03.2008
 *
 * @author jkff
 */
public class UIUtils {
    public static void center(Window w) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - w.getWidth()) / 2;
        int y = (screenSize.height - w.getHeight()) / 2;
        w.setLocation(x, y);
    }

    public static void fixSize(JComponent... items) {
        for (JComponent item : items) {
            item.setMinimumSize(item.getPreferredSize());
            item.setMaximumSize(item.getPreferredSize());
            item.setSize(item.getPreferredSize());
        }
    }
}
