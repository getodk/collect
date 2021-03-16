package org.odk.collect.android.support;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.apache.commons.io.FileUtils.writeStringToFile;

public class FormUtils {

    private FormUtils() {

    }

    public static String createXFormBody(String formId, String version) {
        return createXFormBody(formId, version, "Form");
    }

    public static String createXFormBody(String formId, String version, String title) {
        return "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:orx=\"http://openrosa.org/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <h:head>\n" +
                "        <h:title>" + title + "</h:title>\n" +
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

    public static File createXFormFile(String formId, String version) {
        String body = createXFormBody(formId, version);

        try {
            File file = File.createTempFile(formId + "-" + version, ".xml");
            writeStringToFile(file, body, Charset.defaultCharset());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Form.Builder buildForm(String formId, String version, String formFilesPath) {
        return buildForm(formId, version, formFilesPath, createXFormBody(formId, version));
    }

    public static Form.Builder buildForm(String formId, String version, String formFilesPath, String xform) {
        String fileName = formId + "-" + version + "-" + Math.random();
        File formFile = new File(formFilesPath + "/" + fileName + ".xml");
        FileUtils.write(formFile, xform.getBytes());
        String mediaPath = new File(formFilesPath + "/" + fileName + "-media").getAbsolutePath();

        return new Form.Builder()
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId(formId)
                .jrVersion(version);
    }
}
