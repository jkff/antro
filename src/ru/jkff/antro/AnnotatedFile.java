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

package ru.jkff.antro;

/**
 * Created on 19:16:00 17.03.2008
 *
 * @author jkff
 */
public class AnnotatedFile {
    private String[] lines;
    private Stat[] stats;
    private String name;

    public AnnotatedFile(String[] lines, Stat[] stats, String name) {
        this.lines = lines;
        this.stats = stats;
        this.name = name;
    }

    public int getLineCount() {
        return lines.length;
    }
    public String getLine(int i) {
        return lines[i];
    }
    public Stat getStat(int i) {
        return stats[i];
    }

    public String getName() {
        return name;
    }
}
