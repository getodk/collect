package org.odk.collect.formstest;

import org.apache.commons.io.FileUtils;
import org.odk.collect.forms.Form;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
            FileUtils.writeStringToFile(file, body, Charset.defaultCharset());
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

        try {
            FileUtils.writeByteArrayToFile(formFile, xform.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Form.Builder()
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formId(formId)
                .version(version);
    }
}
