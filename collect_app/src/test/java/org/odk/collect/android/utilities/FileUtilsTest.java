package org.odk.collect.android.utilities;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class FileUtilsTest {
    @Test
    public void md5HashIsCorrect() throws IOException {
        String contents = "Hello, world";
        File tempFile = File.createTempFile("hello", "txt");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);
        fw.write(contents);
        fw.close();
        for (int bufSize : Arrays.asList(1, contents.length() - 1, contents.length(), 64 * 1024)) {
            FileUtils.bufSize = bufSize;
            String expectedResult = "bc6e6f16b8a077ef5fbc8d59d0b931b9";  // From md5 command-line utility
            assertEquals(expectedResult, FileUtils.getMd5Hash(tempFile));
        }
    }

    @Test
    public void mediaDirNameIsCorrect() {
        String expected = "sample-file-media";

        assertEquals(expected, FileUtils.constructMediaPath("sample-file.xml"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.extension"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.123"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.docx"));
    }

    @Test public void getMetadataFromFormDefinition_withoutSubmission_returnsMetaDataFields() throws IOException {
        String simpleForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\"\n" +
                "        xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "        xmlns:orx=\"http://openrosa.org/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>My Survey</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"mysurvey\">\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "\n" +
                "    </h:body>\n" +
                "</h:html>";
        File temp = File.createTempFile("simple_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(simpleForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("My Survey"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("mysurvey"));
        assertThat(metadataFromFormDefinition.get(FileUtils.VERSION), is(nullValue()));
        assertThat(metadataFromFormDefinition.get(FileUtils.BASE64_RSA_PUBLIC_KEY), is(nullValue()));
    }

    @Test public void getMetadataFromFormDefinition_withSubmission_returnsMetaDataFields() throws IOException {
        String submissionForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\"\n" +
                "        xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "        xmlns:orx=\"http://openrosa.org/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>My Survey</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"mysurvey\" orx:version=\"2014083101\">\n" +
                "                    <orx:meta>\n" +
                "                        <orx:instanceID/>\n" +
                "                    </orx:meta>\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <submission action=\"foo\" orx:auto-send=\"bar\" orx:auto-delete=\"baz\" base64RsaPublicKey=\"quux\" />\n" +
                "            <bind nodeset=\"/data/orx:meta/orx:instanceID\" preload=\"uid\" type=\"string\"/>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "\n" +
                "    </h:body>\n" +
                "</h:html>";

        File temp = File.createTempFile("submission_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(submissionForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("My Survey"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("mysurvey"));
        assertThat(metadataFromFormDefinition.get(FileUtils.VERSION), is("2014083101"));
        assertThat(metadataFromFormDefinition.get(FileUtils.SUBMISSIONURI), is("foo"));
        assertThat(metadataFromFormDefinition.get(FileUtils.AUTO_SEND), is("bar"));
        assertThat(metadataFromFormDefinition.get(FileUtils.AUTO_DELETE), is("baz"));
        assertThat(metadataFromFormDefinition.get(FileUtils.BASE64_RSA_PUBLIC_KEY), is("quux"));
    }
}
