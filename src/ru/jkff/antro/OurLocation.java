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
 * Created on 13:04:03 11.03.2008
 *
 * @author jkff
 */
public class OurLocation {
    public final String fileName;
    public final int line;

    public OurLocation(String fileName, int line) {
        this.fileName = fileName;
        this.line = line;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OurLocation)) return false;

        OurLocation that = (OurLocation) o;

        if (line != that.line) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + line;
        return result;
    }
}
