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
 * Created on 12:35:58 11.03.2008
 *
 * @author jkff
 */
public class Stat {
    public int in, out;

    public double avg, min, max, first, total;
    public int count;
    public long tFirst;
    public EventWithCallStack evMin, evMax, evFirst;


    private long began;

    public Stat(double avg, double min, double max, double first, double total, int count, EventWithCallStack evMin, EventWithCallStack evMax, EventWithCallStack evFirst) {
        this.avg = avg;
        this.min = min;
        this.max = max;
        this.first = first;
        this.total = total;
        this.count = count;
        this.evMin = evMin;
        this.evMax = evMax;
        this.evFirst = evFirst;
    }

    public Stat() {
    }

    void begin(int in) {
        if(this.in != 0) {
            throw new AssertionError("Begin should be called only once");
        }

        this.in = in;
        began = System.currentTimeMillis();
    }

    void end(EventWithCallStack ev, int out) {
        this.out = out;
        long t = System.currentTimeMillis();
        long dtMs = t - began;

        double dt = dtMs * 1.0 / 1000;

        count++;
        total += dt;
        avg = total / count;
        if (dt < min || evMin == null) {
            min = dt;
            evMin = ev;
        }
        if (dt > max || evMax == null) {
            max = dt;
            evMax = ev;
        }
        if (count == 1 || evFirst == null) {
            first = dt;
            evFirst = ev;
            tFirst = t;
        }
    }

    public void join(Stat stat) {
        // This line is incorrect if these stats intersect as a result of recursive invocations
        // See ProfileListener.getStatsByLocation - 'total' is fixed up afterwards.
        this.total += stat.total;

        this.count += stat.count;
        this.avg = this.total / this.count;

        if (stat.min < this.min || this.evMin == null) {
            this.min = stat.min;
            this.evMin = stat.evMin;
        }
        if (stat.max > this.max || this.evMax == null) {
            this.max = stat.max;
            this.evMax = stat.evMax;
        }
        if (stat.tFirst < this.tFirst || this.evFirst == null) {
            this.first = stat.first;
            this.evFirst = stat.evFirst;
        }
    }
}
