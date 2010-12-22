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

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Created on 13:43:52 22.03.2008
 *
 * @author jkff
 */
public class TextAttributes extends SimpleAttributeSet {
    public TextAttributes bold() {
        StyleConstants.setBold(this, true);
        return this;
    }


    public static TextAttributes make() {
        return new TextAttributes();
    }

    public boolean isBold() {
        return StyleConstants.isBold(this);
    }
}
