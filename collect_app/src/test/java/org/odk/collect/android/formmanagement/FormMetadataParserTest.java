package org.odk.collect.android.formmanagement;

import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FormMetadataParserTest {

    private File mediaDir;

    @Before
    public void setup() {
        mediaDir = Files.createTempDir();
    }

    @Test
    public void canParseFormWithExternalSecondaryInstance() throws Exception {
        File formXml = File.createTempFile("form", ".xml");
        FileUtils.write(formXml, EXTERNAL_SECONDARY_INSTANCE.getBytes());

        File externalInstance = new File(mediaDir, "external-data.xml");
        FileUtils.write(externalInstance, EXTERNAL_INSTANCE.getBytes());

        FormMetadataParser formMetadataParser = new FormMetadataParser();
        Map<String, String> metaData = formMetadataParser.parse(formXml, mediaDir);
        assertThat(metaData.get(FileUtils.FORMID), is("basic-external-xml-instance"));
    }

    @Test
    public void canParseFormWithCSVExternalSecondaryInstance() throws Exception {
        File formXml = File.createTempFile("form", ".xml");
        FileUtils.write(formXml, CSV_EXTERNAL_SECONDARY_INSTANCE.getBytes());

        File externalInstance = new File(mediaDir, "external-data.csv");
        FileUtils.write(externalInstance, CSV_EXTERNAL_INSTANCE.getBytes());

        FormMetadataParser formMetadataParser = new FormMetadataParser();
        Map<String, String> metaData = formMetadataParser.parse(formXml, mediaDir);
        assertThat(metaData.get(FileUtils.FORMID), is("basic-external-csv-instance"));
    }

    @Test
    public void canParseFormWithLastSaved() throws Exception {
        File formXml = File.createTempFile("form", ".xml");
        FileUtils.write(formXml, LAST_SAVED.getBytes());

        FormMetadataParser formMetadataParser = new FormMetadataParser();
        Map<String, String> metaData = formMetadataParser.parse(formXml, mediaDir);
        assertThat(metaData.get(FileUtils.FORMID), is("basic-last-saved"));
    }

    @Test
    public void doesNotLeaveFilesInMediaDir() throws Exception {
        File formXml = File.createTempFile("form", ".xml");
        FileUtils.write(formXml, LAST_SAVED.getBytes());

        FormMetadataParser formMetadataParser = new FormMetadataParser();
        formMetadataParser.parse(formXml, mediaDir);

        assertThat(mediaDir.listFiles().length, is(0));
    }

    private static final String EXTERNAL_SECONDARY_INSTANCE = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" >\n" +
            "    <h:head>\n" +
            "        <h:title>External Secondary Instance</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"basic-external-xml-instance\">\n" +
            "                    <first/>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <instance id=\"external-xml\" src=\"jr://file/external-data.xml\" />\n" +
            "            <bind nodeset=\"/data/first\" type=\"select1\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <select1 ref=\"/data/first\">\n" +
            "            <label>First</label>\n" +
            "            <itemset nodeset=\"instance('external-xml')/root/item[first='']\">\n" +
            "                <value ref=\"name\"/>\n" +
            "                <label ref=\"label\"/>\n" +
            "            </itemset>\n" +
            "        </select1>\n" +
            "    </h:body>\n" +
            "</h:html>";

    private static final String EXTERNAL_INSTANCE = "<root>\n" +
            "    <item>\n" +
            "        <label>A</label>\n" +
            "        <name>a</name>\n" +
            "    </item>\n" +
            "    <item>\n" +
            "        <label>B</label>\n" +
            "        <name>b</name>\n" +
            "    </item>\n" +
            "    <item>\n" +
            "        <label>C</label>\n" +
            "        <name>c</name>\n" +
            "    </item>\n" +
            " </root>";

    private static final String CSV_EXTERNAL_SECONDARY_INSTANCE = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" >\n" +
            "    <h:head>\n" +
            "        <h:title>basic-external-csv-instance</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"basic-external-csv-instance\">\n" +
            "                    <first/>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <instance id=\"external-csv\" src=\"jr://file-csv/external-data.csv\" />\n" +
            "            <bind nodeset=\"/data/first\" type=\"select1\"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <select1 ref=\"/data/first\">\n" +
            "            <label>First</label>\n" +
            "            <itemset nodeset=\"instance('external-csv')/root/item[first='']\">\n" +
            "                <value ref=\"name\"/>\n" +
            "                <label ref=\"label\"/>\n" +
            "            </itemset>\n" +
            "        </select1>\n" +
            "    </h:body>\n" +
            "</h:html>";

        private static final String CSV_EXTERNAL_INSTANCE = "label,name\n" +
                "A, a\n" +
                "B, b\n" +
                "C, c\n";

    private static final String LAST_SAVED = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
            "    <h:head>\n" +
            "        <h:title>basic-last-saved</h:title>\n" +
            "        <model>\n" +
            "            <instance>\n" +
            "                <data id=\"basic-last-saved\">\n" +
            "                    <q1/>\n" +
            "                </data>\n" +
            "            </instance>\n" +
            "            <instance id=\"__last-saved\" src=\"jr://instance/last-saved\"/>\n" +
            "            <bind nodeset=\"/data/q1\" type=\"string\"/>\n" +
            "           <setvalue event=\"odk-instance-first-load\" ref=\"/data/q1\" value=\" instance('__last-saved')/data/q1 \"/>\n" +
            "        </model>\n" +
            "    </h:head>\n" +
            "    <h:body>\n" +
            "        <input ref=\"/data/q1\">\n" +
            "            <label>Question</label>\n" +
            "        </input>\n" +
            "    </h:body>\n" +
            "</h:html>";
}