package org.odk.collect.android.support;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;

public class FormUtils {

    private FormUtils() {

    }

    public static String createXForm(String formId, String version) {
        return "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <h:head>\n" +
                "        <h:title>Form</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"" + formId + "\" orx:version=\"" + version + "\">\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "    </h:body>\n" +
                "</h:html>";
    }

    public static Form.Builder buildForm(Long id, String formId, String version, String formFilesPath) {
        return buildForm(id, formId, version, formFilesPath, "blah");
    }

    public static Form.Builder buildForm(Long id, String formId, String version, String formFilesPath, String xform) {
        String fileName = formId + "-" + version + "-" + Math.random();
        File formFile = new File(formFilesPath + "/" + fileName + ".xml");
        FileUtils.write(formFile, xform.getBytes());
        String mediaPath = new File(formFilesPath + "/" + fileName + "-media").getAbsolutePath();

        return new Form.Builder()
                .id(id)
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId(formId)
                .jrVersion(version);
    }
}
