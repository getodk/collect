package org.odk.collect.android.loaders;

/**
 * This class holds the per-item data in the {@link TaskLoader}.
 */
public class TaskEntry {
    public long id;
    public String type;    // form or task
    public String taskStatus;
    public boolean repeat;  // A task that can be repeated multiple times
    public String name;
    public String displayName;
    public String project;
    public String ident;
    public long taskStart;          // Scheduled time of task
    public String taskAddress;
    public String taskForm;
    public String jrFormId;
    public String instancePath;
    public int formVersion;
    public double schedLon = 0.0;
    public double schedLat = 0.0;
    public double actLon = 0.0;
    public double actLat = 0.0;
    public long actFinish;          // Date time the task was finalised
    public String isSynced;
    public long assId;
    public String uuid;
    public String source;
    public String locationTrigger;



    @Override
    public String toString() {
        return taskStatus;
    }

}