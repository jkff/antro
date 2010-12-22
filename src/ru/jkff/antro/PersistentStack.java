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
 * Created on 13:22:12 11.03.2008
 *
 * @author jkff
 */
public class PersistentStack<T> {
    private T head;
    private PersistentStack<T> tail;

    private static PersistentStack NULL = new PersistentStack();
    private PersistentStack() {
    }

    private PersistentStack(T head, PersistentStack<T> tail) {
        this.head = head;
        this.tail = tail;
    }


    public boolean isEmpty() {
        return this==NULL;
    }

    public static <T> PersistentStack<T> empty() {
        return NULL;
    }

    public PersistentStack<T> push(T item) {
        return new PersistentStack<T>(item, this);
    }

    public T peek() {
        return head;
    }

    public PersistentStack<T> pop() {
        return tail;
    }

    public List<T> toList() {
        List<T> res = new ArrayList<T>();
        for(PersistentStack<T> s = this; s != NULL; s = s.pop()) {
            res.add(s.peek());
        }
        return res;
    }
}
