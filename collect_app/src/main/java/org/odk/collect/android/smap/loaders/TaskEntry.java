package org.odk.collect.android.smap.loaders;

import org.javarosa.core.model.FormIndex;

/**
 * This class holds the per-item data for items to show on the task list and map
 */
public class TaskEntry {
    public long id;
    public String type;    // form or task
    public String taskStatus;
    public String taskComment;  // Comment added by a user
    public boolean repeat;  // A task that can be repeated multiple times
    public String name;
    public String displayName;
    public String project;
    public String ident;
    public long taskStart;          // Scheduled time of task
    public long taskFinish;         // Scheduled finish time of task
    public String taskAddress;
    public String taskForm;
    public String jrFormId;
    public String instancePath;
    public int formVersion;
    public double schedLon = 0.0;
    public double schedLat = 0.0;
    public double actLon = 0.0;
    public double actLat = 0.0;
    public int showDist = 0;
    public long actFinish;          // Date time the task was finalised
    public String isSynced;
    public long assId;
    public String uuid;
    public String source;
    public String locationTrigger;
    public FormIndex formIndex;     // If this is a restart to a form that is being edited
    public String instanceId;
    public String formStatus;
    public String formURI;
    //public String geometryXPath;  // Disable



    @Override
    public String toString() {
        return taskStatus;
    }

}