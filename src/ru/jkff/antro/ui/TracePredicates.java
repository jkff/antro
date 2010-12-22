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

import ru.jkff.antro.Call;
import ru.jkff.antro.OurLocation;
import ru.jkff.antro.Trace;

/**
 * Created on 14:18:05 22.03.2008
 *
 * @author jkff
 */
public class TracePredicates {
    public static Predicate<Trace> byLocation(final OurLocation location) {
        return new Predicate<Trace>() {
            public boolean fits(Trace trace) {
                return trace.getCall().location.equals(location);
            }
        };
    }

    public static Predicate<Trace> byFile(final String fileName) {
        return new Predicate<Trace>() {
            public boolean fits(Trace trace) {
                return trace.getCall().location.fileName.equals(fileName);
            }
        };
    }

    public static Predicate<Trace> byTask(final String taskName) {
        return new Predicate<Trace>() {
            public boolean fits(Trace trace) {
                return trace.getCall().kind == Call.Kind.TASK && trace.getCall().name.equals(taskName);
            }
        };
    }

    public static Predicate<Trace> byTarget(final String targetName) {
        return new Predicate<Trace>() {
            public boolean fits(Trace trace) {
                return trace.getCall().kind == Call.Kind.TARGET && trace.getCall().name.equals(targetName);
            }
        };
    }
}
