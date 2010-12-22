package ru.jkff.antro;

import org.apache.tools.ant.*;

import java.io.IOException;

/**
 * Created on 14:01:20 02.10.2008
 *
 * @author jkff
 */
public class Replayer {
    public static void main(String[] args) throws IOException {
        ReportReader rr = new ReportReader();
        Report report = rr.readReport(args[0]);
        ProfileListener listener = new ProfileListener();
        listener.setDontWriteFile(true);
        replayTrace(report.getTrace(), listener);
    }

    private static void replayTrace(Trace trace, ProfileListener listener) {
        Call call = trace.getCall();
        Project project = null;
        Target target = null;
        Task task = null;
        switch (call.kind) {
        case BUILD:
            project = new Project();
            listener.buildStarted(new BuildEvent(project));
            break;
        case SUBBUILD:
            project = new Project();
            listener.subBuildStarted(new BuildEvent(project));
            break;
        case TARGET:
            target = new Target();
            target.setName(call.name);
            target.setLocation(new Location(call.location.fileName, call.location.line, 0));
            listener.targetStarted(new BuildEvent(target));
            break;
        case TASK:
            task = new Task() {};
            task.setTaskName(call.name);
            task.setLocation(new Location(call.location.fileName, call.location.line, 0));
            listener.taskStarted(new BuildEvent(task));
            break;
        }

        for(Trace t : trace.getChildren()) {
            replayTrace(t, listener);
        }

        switch (call.kind) {
        case BUILD:
            listener.buildFinished(new BuildEvent(project));
            break;
        case SUBBUILD:
            listener.subBuildFinished(new BuildEvent(project));
            break;
        case TARGET:
            listener.targetFinished(new BuildEvent(target));
            break;
        case TASK:
            listener.taskFinished(new BuildEvent(task));
            break;
        }
    }
}
