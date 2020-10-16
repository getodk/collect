package org.odk.collect.android.support;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;

public class FormUtils {

    private FormUtils() {

    }

    public static String createXForm(String id, String version) {
        return "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <h:head>\n" +
                "        <h:title>Form</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"" + id + "\" orx:version=\"" + version + "\">\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "    </h:body>\n" +
                "</h:html>";
    }

    public static Form.Builder buildForm(long id, String jrFormId, String jrVersion, String formFilesPath) {
        return buildForm(id, jrFormId, jrVersion, formFilesPath, "blah");
    }

    public static Form.Builder buildForm(long id, String jrFormId, String jrVersion, String formFilesPath, String xform) {
        String fileName = jrFormId + "-" + jrVersion;
        File formFile = new File(formFilesPath + "/" + fileName + ".xml");
        FileUtils.write(formFile, xform.getBytes());
        String mediaPath = new File(formFilesPath + "/" + fileName + "-media").getAbsolutePath();

        return new Form.Builder()
                .id(id)
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId(jrFormId)
                .jrVersion(jrVersion)
                .md5Hash(FileUtils.getMd5Hash(formFile));
    }
}
