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

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 10:16:07 12.03.2008
 *
 * @author jkff
 */
public class Trace {
    private PersistentStack<Call> callStack = PersistentStack.empty();

    private final List<Trace> children = new ArrayList<Trace>();
    private final Trace parent;
    private final Call call;

    double totalTime;
    double childrenTime;

    private long enterTime;

    public Trace(Trace parent, Call call) {
        this.enterTime = System.currentTimeMillis();
        this.call = call;
        this.parent = parent;
        if(parent == null) {
            callStack = PersistentStack.empty();
        } else {
            callStack = parent.callStack.push(call);
        }
    }

    public static Trace newRoot(String name, OurLocation location) {
        return new Trace(null, new Call(Call.Kind.BUILD, name, location));
    }

    public Trace enter(Call call) {
        Trace child = new Trace(this, call);
        children.add(child);
        return child;
    }

    public Trace leave() {
        totalTime = (System.currentTimeMillis() - enterTime) / 1000.0;

        if(parent != null)
            parent.childrenTime += totalTime;
        return parent;
    }

    public Call getCall() {
        return call;
    }

    public List<Trace> getChildren() {
        return children;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getChildrenTime() {
        return childrenTime;
    }

    public double getOwnTime() {
        return getTotalTime() - getChildrenTime();
    }
    
    public double getPercentOfParent() {
        if (parent == null) return 100.0;
        if (parent.totalTime == 0) return 0.0;

        else return ((100.0 * totalTime) / parent.totalTime);
    }

    public PersistentStack<Call> getCallStack() {
        return callStack;
    }

    void addChild(Trace trace) {
        children.add(trace);
    }
}
