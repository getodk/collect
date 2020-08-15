package org.odk.collect.android.support;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;

public class FormUtils {

    private FormUtils() {

    }

    public static Form.Builder buildForm(long id, String jrFormId, String jrVersion, String formFilesPath) {
        String fileName = jrFormId + "-" + jrVersion;
        File formFile = new File(formFilesPath + "/" + fileName + ".xml");
        FileUtils.write(formFile, "blah".getBytes());
        String mediaPath = new File(formFilesPath + "/" + fileName + "-media").getAbsolutePath();

        return new Form.Builder()
                .id(id)
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId(jrFormId)
                .jrVersion(jrVersion);
    }
}
