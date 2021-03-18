package org.odk.collect.android.taskModel;

/**
 * Created by neilpenman on 16/11/2014.
 */
public class TaskCompletionInfo {
    public long assId;
    public double lat;
    public double lon;
    public long actFinish;	// When the task was finished
    public String ident;	// Survey ident
    public int version;     // Form version
    public String uuid;		// Unique identifier for the results
}
