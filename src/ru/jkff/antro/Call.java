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
 * Created on 13:21:14 11.03.2008
 *
 * @author jkff
 */
public class Call {
    public enum Kind {
        BUILD,
        SUBBUILD,
        TARGET,
        TASK
    }
    public final Kind kind;
    public final String name;
    public final OurLocation location;

    public Call(Kind kind, String name, OurLocation location) {
        this.kind = kind;
        this.name = name;
        this.location = location;
    }
}
