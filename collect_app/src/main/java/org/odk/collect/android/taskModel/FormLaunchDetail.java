package org.odk.collect.android.taskModel;

public class FormLaunchDetail {
	
	public long id;
	public String instancePath;
	
	public FormLaunchDetail(long id) {
	    this.id = id;
    }
    public FormLaunchDetail(String instancePath) {
        this.instancePath = instancePath;
    }
}
