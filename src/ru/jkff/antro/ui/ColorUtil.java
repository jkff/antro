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

import java.awt.*;

/**
 * Created on 15:39:00 22.03.2008
 *
 * @author jkff
 */
public class ColorUtil {
    public static Color blend(Color a, Color b, double val) {
        return new Color(
                (int)(b.getRed()*val   + a.getRed()*(1-val)),
                (int)(b.getGreen()*val + a.getGreen()*(1-val)),
                (int)(b.getBlue()*val  + a.getBlue()*(1-val))
                );
    }
}
