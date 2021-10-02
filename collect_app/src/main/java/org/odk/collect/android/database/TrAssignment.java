package org.odk.collect.android.database;

public class TrAssignment {
	public int assignment_id;           // This is the task_id (However server uses assignment_id)
	public String assignment_status;    // task_status
    public String task_comment;    // task_status
	public int task_id;				// included to match server definition
	public long dbId;
	public String uuid;             // The instance id of the submitted record associated with this task
}
