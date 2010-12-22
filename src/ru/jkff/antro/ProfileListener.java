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

import org.apache.tools.ant.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created on 12:36:50 11.03.2008
 *
 * @author jkff
 */
public class ProfileListener implements SubBuildListener {
    private Trace trace;

    private Project project;

    private int numEvents = 0;
    private boolean dontWriteFile;

    protected Call createCall(BuildEvent event, Call.Kind kind) {
        return new Call(kind, event2name(kind, event), event2location(kind, event));
    }

    public void subBuildStarted(BuildEvent event) {
        numEvents++;
        Call call = createCall(event, Call.Kind.SUBBUILD);
        trace = trace.enter(call);
    }

    public void subBuildFinished(BuildEvent event) {
        numEvents++;
        trace = trace.leave();
    }

    public void buildStarted(BuildEvent event) {
        numEvents++;
        project = event.getProject();

        System.out.println("Build started: " + project.getName());

        trace = Trace.newRoot(event2name(Call.Kind.BUILD, event), event2location(Call.Kind.BUILD, event));
    }

    public void buildFinished(BuildEvent event) {
        numEvents++;
        trace.leave();

        String fileName = getReportFileName();

        Report report = getReport();

        dumpReport(report, fileName);
    }

    public void targetStarted(BuildEvent event) {
        numEvents++;
        Call.Kind kind = Call.Kind.TARGET;
        trace = trace.enter(createCall(event, kind));
    }

    public void targetFinished(BuildEvent event) {
        numEvents++;
        trace = trace.leave();
    }

    public void taskStarted(BuildEvent event) {
        numEvents++;
        trace = trace.enter(createCall(event, Call.Kind.TASK));
        getTaskStat(event).begin(numEvents);
    }

    public void taskFinished(BuildEvent event) {
        numEvents++;
        getTaskStat(event).end(new EventWithCallStack(trace.getCallStack()), numEvents);
        trace = trace.leave();
    }

    public void messageLogged(BuildEvent event) {
        numEvents++;
    }
    

    private Report report;

    public Report getReport() {
        if (report == null)
            report = new Report(trace, getAnnotatedFiles());
        return report;
    }

    private Map<String, AnnotatedFile> getAnnotatedFiles() {
        Map<String, AnnotatedFile> res = new HashMap<String, AnnotatedFile>();

        Map<OurLocation, Stat> statsByLoc = getStatsByLocation();
        for (String file : getUsedBuildFiles()) {
            List<String> lines = new ArrayList<String>();
            List<Stat> stats = new ArrayList<Stat>();

            try {
                LineNumberReader r = new LineNumberReader(new FileReader(file));
                String line;
                while (null != (line = r.readLine())) {
                    OurLocation loc = new OurLocation(file, r.getLineNumber());

                    Stat stat = statsByLoc.get(loc);
                    lines.add(line);
                    stats.add(stat);
                }
            } catch (IOException e) {
            }

            res.put(file, new AnnotatedFile(lines.toArray(new String[0]), stats.toArray(new Stat[0]), file));
        }

        return res;
    }

    private String getReportFileName() {
        SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
        String dateString = ISO8601FORMAT.format(new Date());

        String dir = project.getProperty("antro.dir");
        if (dir == null || "".equals(dir))
            dir = project.getUserProperty("antro.dir");
        if (dir == null || "".equals(dir))
            dir = ".";
        return dir + "/antro-report-" + dateString + ".json";
    }

    public void dumpReport(Report report, String reportFilename) {
        try {
            JSONArray res = new JSONArray();

            for (String file : report.getUsedBuildFiles()) {
                AnnotatedFile f = report.getAnnotatedFile(file);

                JSONArray annotatedFile = new JSONArray();
                for (int i = 0; i < f.getLineCount(); ++i) {
                    String line = f.getLine(i);
                    Stat stat = f.getStat(i);
                    annotatedFile.put(annotateLine(line, i, stat));
                }

                JSONObject fileObj = new JSONObject();
                fileObj.put("name", file);
                fileObj.put("stat", annotatedFile);

                res.put(fileObj);
            }

            if(!dontWriteFile) {
                FileWriter w = new FileWriter(reportFilename);
                w.write("(\n");
                JSONArray data = new JSONArray();
                data.put(res);
                data.put(toJSON(report.getTrace()));
                data.writeSelfTo(2, 0, w);
                w.write("\n)\n");

                w.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject annotateLine(String line, int lineNumber, Stat s) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("text", escapeTags(line));
        res.put("line", lineNumber);
        if (s != null) res.put("stat", toJSON(s));
        return res;
    }

    private String escapeTags(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private JSONObject toJSON(Stat s) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("total", s.total);
        res.put("count", s.count);
        res.put("avg", s.avg);
        res.put("first", s.first);
        res.put("evFirst", toJSON(s.evFirst));
        res.put("max", s.max);
        res.put("evMax", toJSON(s.evMax));
        res.put("min", s.min);
        res.put("evMin", toJSON(s.evMin));
        return res;
    }

    private JSONObject toJSON(EventWithCallStack e) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("stack", toJSON(e.stack.toList()));
        return res;
    }

    private JSONArray toJSON(List<Call> stack) throws JSONException {
        JSONArray res = new JSONArray();
        for (Call element : stack) {
            res.put(toJSON(element));
        }
        return res;
    }

    private JSONObject toJSON(Call e) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("kind", e.kind.toString());
        res.put("location", toJSON(e.location));
        res.put("name", e.name == null ? JSONObject.NULL : e.name);

        return res;
    }

    private String event2name(Call.Kind kind, BuildEvent event) {
        switch (kind) {
            case BUILD:
                return event.getProject().getName();
            case SUBBUILD:
                return event.getProject().getName();
            case TARGET:
                return event.getTarget().getName();
            case TASK:
                return event.getTask().getTaskName();
            default:
                throw new AssertionError();
        }
    }

    private OurLocation event2location(Call.Kind kind, BuildEvent event) {
        switch (kind) {
            case BUILD:
                return new OurLocation(event.getProject().getName(), 0);
            case SUBBUILD:
                return new OurLocation(event.getProject().getName(), 0);
            case TARGET:
                Location targetLoc = event.getTarget().getLocation();
                return new OurLocation(targetLoc.getFileName(), targetLoc.getLineNumber());
            case TASK:
                Location taskLoc = event.getTask().getLocation();
                return new OurLocation(taskLoc.getFileName(), taskLoc.getLineNumber());
            default:
                throw new AssertionError();
        }
    }

    private JSONObject toJSON(OurLocation loc) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("file", loc.fileName == null ? JSONObject.NULL : loc.fileName);
        res.put("line", loc.line);
        return res;
    }

    private JSONObject toJSON(Trace trace) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("call", toJSON(trace.getCall()));
        if (trace.getTotalTime() > 0)
            res.put("total", trace.getTotalTime());
        if (trace.getOwnTime() > 0)
            res.put("own", trace.getOwnTime());
        if (trace.getPercentOfParent() > 0)
            res.put("percentOfParent", trace.getPercentOfParent());

        JSONArray children = new JSONArray();
        for (Trace child : trace.getChildren()) {
            children.put(toJSON(child));
        }
        if (children.length() > 0)
            res.put("children", children);

        return res;
    }

    private Set<String> getUsedBuildFiles() {
        Set<String> res = new HashSet<String>();
        for (Task task : statsByTask.keySet()) {
            String file = task.getLocation().getFileName();
            if (file != null)
                res.add(file);
        }
        return res;
    }

    private Map<OurLocation, Stat> getStatsByLocation() {
        Map<OurLocation, Stat> res = new HashMap<OurLocation, Stat>();

        // Special care should be taken for recursive calls:
        // If Stat's are joined in the usual way, 'total time' is computed for a line incorrectly
        // if the line calls itself as a result of a recursive call (in case of subbuilds or
        // antcallbacks, for instance)
        // So we'll remember the whole list of in/out dfs numbers for a line and afterwards
        // use some freakin' crazy magic algorithmic stuff to compute total time correctly!
        Map<OurLocation, List<Stat>> recTrees = new HashMap<OurLocation,List<Stat>>();

        for (Task task : statsByTask.keySet()) {
            Stat taskStat = statsByTask.get(task);
            OurLocation loc = new OurLocation(task.getLocation().getFileName(), task.getLocation().getLineNumber());

            Stat s = res.get(loc);
            if (s == null) {
                res.put(loc, s = new Stat());
            }
            s.join(taskStat);

            List<Stat> stats = recTrees.get(loc);
            if(stats == null) {
                recTrees.put(loc, stats = new ArrayList<Stat>());
            }
            stats.add(taskStat);
        }

        for(OurLocation loc : res.keySet()) {
            res.get(loc).total = computeTotalTimeCorrectly(recTrees.get(loc));
        }

        return res;
    }

    // Compute the correct total time over a list of possibly-recursively-overlapping stats
    private static double computeTotalTimeCorrectly(List<Stat> stats) {
        // Sort stats by 'in' and traverse them
        // At each point, we either are within an invocation
        // (and we know when it began and when it will end - in terms of dfs indices), or we aren't.
        // Skip recursive stats and add up totals of the top-level ones.

        Collections.sort(stats, new Comparator<Stat>() {
            public int compare(Stat o1, Stat o2) {
                return o1.in < o2.in ? -1 :
                       o1.in > o2.in ? 1  : 
                       0;
            }
        });

        int out = 0;
        double total = 0;
        for(Stat stat : stats) {
            if(stat.out < out) // A recursive invocation
                 continue;
            out = stat.out;
            total += stat.total;
        }
        return total;
    }

    // 'task' means 'task invocation'.
    private Map<Task, Stat> statsByTask = new HashMap<Task, Stat>();

    private Stat getTaskStat(BuildEvent ev) {
        Stat s = statsByTask.get(ev.getTask());
        if (s == null) {
            statsByTask.put(ev.getTask(), s = new Stat());
        }
        return s;
    }

    public void setDontWriteFile(boolean dontWriteFile) {
        this.dontWriteFile = dontWriteFile;
    }
}
