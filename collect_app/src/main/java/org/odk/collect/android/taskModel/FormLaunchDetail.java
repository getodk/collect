package org.odk.collect.android.taskModel;

import org.javarosa.core.model.FormIndex;

public class FormLaunchDetail {
	
	public long id;
	public String instancePath;
	public FormIndex formIndex;
	public String formName;
	public String initialData;
	
	public FormLaunchDetail(long id, String formName, String initialData) {
	    this.id = id;
	    this.formName = formName;
        this.initialData = initialData;
    }
    public FormLaunchDetail(String instancePath, FormIndex formIndex, String formName) {
        this.instancePath = instancePath;
        this.formIndex = formIndex;
        this.formName = formName;
    }
}
