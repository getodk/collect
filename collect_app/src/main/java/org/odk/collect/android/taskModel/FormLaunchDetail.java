package org.odk.collect.android.taskModel;

import org.javarosa.core.model.FormIndex;

public class FormLaunchDetail {
	
	public long id;
	public String instancePath;
	public FormIndex formIndex;
	
	public FormLaunchDetail(long id) {
	    this.id = id;
    }
    public FormLaunchDetail(String instancePath, FormIndex formIndex) {
        this.instancePath = instancePath;
        this.formIndex = formIndex;
    }
}
