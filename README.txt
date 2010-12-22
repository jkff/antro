---- What is this? ----
Antro is a profiler for ant build scripts. It allows to view
a complete trace of the build annotated with timing information
and to view by-line statistics for the build scripts involved.

---- Usage ----
To profile a build script, one has to
1) run ant with antro attached as a listener; this will produce a report
2) run antro GUI and open the report
3) stare at the report until the bottlenecks become clear

1) Do something like this:

   ant -listener ru.jkff.antro.ProfileListener -lib ~/antro/antro.jar build.xml

The report will be written to the current directory, unless overridden with

   -Dantro.dir=/where/to/write/the/report.

2) Just run antro.jar with

   java -jar antro.jar

or double-click antro.jar (in Windows).

3) The GUI is rather obvious to use: it has a trace view, a bar view and
a line view.

   The trace view is a trace of the whole build, annotated with how much time
each task/target/subbuild took.
   Builds/subbuilds look like [my-project]
   Targets look like #compile-and-jar
   Tasks look like <javac>

   The bar view shows an overview of the selected trace node. Each bar
corresponds to a trace node and can be hovered, clicked or double-clicked.
   When hovered, a tooltip appears.
   When clicked, the node is highlighted in the bar view and also highlighted
are usages of the same source line in the bar view.
   When double-clicked, the double-clicked node becomes root for the bar view
and is selected in the tree.

   The line view speaks for itself.

   Keys: (inspired by IntelliJ IDEA (tm))
-  F4 in trace: Select corresponding source line in the line view
-  Ctrl-Shift-F7 in trace or line view: highlight usages of the corresponding
   line in the trace

---- History ----

02 Oct 2008 - version 0.52
    Accepted patch: format filenames as Windows-friendly ISO8601 - thanks, my anonymous friend!
    By-line statistics for recursive tasks is now correct
    Todo section added below

04 Apr 2008 - version 0.51
    Fixed bug with handling of -Dantro.dir=.. (it was not handled at all)

(not long before that) - initial release       

---- Todo ----
In order of decreasing priority
 - Show time statistics by leaf tasks, sorted by time percent (like, javac took 80% of the time)
 - Show a vertical scrollbar in the bar view
 - Support displaying the min/max/first call stacks for a line on a double-click on the corresponding
   column (the info is present in the json report, it is just not shown)
 - Support F3/Shift-F3 keys for 'cycle between usages'
