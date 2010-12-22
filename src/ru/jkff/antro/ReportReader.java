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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * Created on 13:39:02 17.03.2008
 *
 * @author jkff
 */
public class ReportReader {
    public Report readReport(String filename) throws IOException {
        // function getProfileData() {
        // return (
        //   JSONObject (read until {} [] braces are balanced)
        // )
        // }
        // function getTrace() {
        // return (
        //   JSONObject (read until {} [] braces are balanced)
        // )
        // }
        try {
            LineNumberReader r = new LineNumberReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line;
            while (null != (line = r.readLine())) {
                sb.append(line);
            }

            JSONArray data = new JSONArray(sb.toString()).getJSONArray(0);

            JSONArray profileData = data.getJSONArray(0);
            JSONObject traceData = data.getJSONObject(1);

            return new Report(toTrace(traceData), toAnnotatedFiles(profileData));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, AnnotatedFile> toAnnotatedFiles(JSONArray profileData) throws JSONException {
        Map<String,AnnotatedFile> res = new HashMap<String, AnnotatedFile>();
        for (int i = 0; i < profileData.length(); ++i) {
            String name = profileData.getJSONObject(i).getString("name");
            res.put(name,
                    toAnnotatedFile(profileData.getJSONObject(i).getJSONArray("stat"), name));
        }
        return res;
    }

    private AnnotatedFile toAnnotatedFile(JSONArray fileStat, String fileName) throws JSONException {
        String[] lines = new String[fileStat.length()];
        Stat[] stats = new Stat[fileStat.length()];

        for (int i = 0; i < fileStat.length(); ++i) {
            JSONObject line = fileStat.getJSONObject(i);
            lines[i] = line.getString("text");
            stats[i] = line.has("stat") ? toStat(line.getJSONObject("stat")) : null;
        }

        return new AnnotatedFile(lines, stats, fileName);
    }

    private Stat toStat(JSONObject stat) throws JSONException {
        double avg = stat.getDouble("avg");
        double min = stat.getDouble("min");
        double max = stat.getDouble("max");
        double first = stat.getDouble("first");
        double total = stat.getDouble("total");
        int count = stat.getInt("count");

        PersistentStack<Call> smin = toStack(stat.getJSONObject("evMin").getJSONArray("stack"));
        PersistentStack<Call> smax = toStack(stat.getJSONObject("evMax").getJSONArray("stack"));
        PersistentStack<Call> sfirst = toStack(stat.getJSONObject("evFirst").getJSONArray("stack"));

        EventWithCallStack evMin = new EventWithCallStack(smin);
        EventWithCallStack evMax = new EventWithCallStack(smax);
        EventWithCallStack evFirst = new EventWithCallStack(sfirst);

        return new Stat(avg, min, max, first, total, count, evMin, evMax, evFirst);
    }

    private PersistentStack<Call> toStack(JSONArray stack) throws JSONException {
        List<Call> calls = new ArrayList<Call>();
        for (int i = 0; i < stack.length(); ++i) {
            calls.add(toCall(stack.getJSONObject(i)));
        }

        PersistentStack<Call> res = PersistentStack.empty();
        Collections.reverse(calls);
        for (Call call : calls) {
            res = res.push(call);
        }

        return res;
    }

    private Call toCall(JSONObject call) throws JSONException {
        Call.Kind kind = Call.Kind.valueOf(call.getString("kind"));
        return new Call(kind, call.getString("name"), toLocation(call.getJSONObject("location")));
    }

    private OurLocation toLocation(JSONObject location) throws JSONException {
        return new OurLocation(location.has("file") ? location.getString("file") : "(unknown file)", location.getInt("line"));
    }

    private Trace toTrace(JSONObject root) throws JSONException {
        String name = root.getJSONObject("call").getString("name");
        Trace t = Trace.newRoot(name, new OurLocation(name, 0));
        double total = root.has("total") ? root.getDouble("total") : 0;
        double own = root.has("own") ? root.getDouble("own") : 0;

        t.totalTime = total;
        t.childrenTime = total - own;

        if (root.has("children")) {
            JSONArray children = root.getJSONArray("children");
            for (int i = 0; i < children.length(); ++i) {
                t.addChild(toTrace(children.getJSONObject(i), t));
            }
        }
        return t;
    }

    private Trace toTrace(JSONObject root, Trace parent) throws JSONException {
        Call call = toCall(root.getJSONObject("call"));
        Trace t = new Trace(parent, call);
        double total = root.has("total") ? root.getDouble("total") : 0;
        double own = root.has("own") ? root.getDouble("own") : 0;

        t.totalTime = total;
        t.childrenTime = total - own;

        if (root.has("children")) {
            JSONArray children = root.getJSONArray("children");
            for (int i = 0; i < children.length(); ++i) {
                t.addChild(toTrace(children.getJSONObject(i), t));
            }
        }
        return t;
    }

    private Set<String> getUsedBuildFiles(Map<OurLocation, Stat> statsByLocation) {
        Set<String> res = new HashSet<String>();
        for (OurLocation loc : statsByLocation.keySet()) {
            res.add(loc.fileName);
        }
        return res;
    }
}
