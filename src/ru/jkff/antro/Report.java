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

import java.util.Collection;
import java.util.Map;

/**
 * Created on 13:39:18 17.03.2008
 *
 * @author jkff
 */
public class Report {
    private final Trace trace;
    private final Map<String, AnnotatedFile> annotatedFiles;

    public Report(Trace trace, Map<String, AnnotatedFile> annotatedFiles) {
        this.trace = trace;
        this.annotatedFiles = annotatedFiles;
    }

    public Trace getTrace() {
        return trace;
    }

    public Collection<String> getUsedBuildFiles() {
        return annotatedFiles.keySet();
    }

    public AnnotatedFile getAnnotatedFile(String file) {
        return annotatedFiles.get(file);
    }
}
