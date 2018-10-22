package org.odk.collect.android.taskModel;

import org.javarosa.core.model.FormIndex;

public class FormLaunchDetail {
	
	public long id;
	public String instancePath;
	public FormIndex formIndex;
	public String formName;
	
	public FormLaunchDetail(long id, String formName) {
	    this.id = id;
	    this.formName = formName;
    }
    public FormLaunchDetail(String instancePath, FormIndex formIndex, String formName) {
        this.instancePath = instancePath;
        this.formIndex = formIndex;
        this.formName = formName;
    }
}
