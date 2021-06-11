package org.odk.collect.android.utilities;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUtilsTest {

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
        assertThat(metadataFromFormDefinition.get(FileUtils.GEOMETRY_XPATH), is(nullValue()));
    }

    @Test public void getMetadataFromFormDefinition_withGeopointsAtTopLevel_returnsFirstGeopointBasedOnBodyOrder() throws IOException {
        String submissionForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "    xmlns=\"http://www.w3.org/2002/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>Two geopoints</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"two-geopoints\">\n" +
                "                    <location2 />\n" +
                "                    <name />\n" +
                "                    <location1 />\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/name\" type=\"string\" />\n" +
                "            <bind nodeset=\"/data/location2\" type=\"geopoint\" />\n" +
                "            <bind nodeset=\"/data/location1\" type=\"geopoint\" />\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <input ref=\"/data/location1\"> <label>Location</label> </input>\n" +
                "        <input ref=\"/data/name\"> <label>Name</label> </input>\n" +
                "        <input ref=\"/data/location2\"> <label>Location</label> </input>\n" +
                "    </h:body>\n" +
                "</h:html>";

        File temp = File.createTempFile("geopoints_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(submissionForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("Two geopoints"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("two-geopoints"));
        assertThat(metadataFromFormDefinition.get(FileUtils.GEOMETRY_XPATH), is("/data/location1"));
    }

    @Test public void getMetadataFromFormDefinition_withGeopointInGroup_returnsFirstGeopointBasedOnBodyOrder() throws IOException {
        String submissionForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "    xmlns=\"http://www.w3.org/2002/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>Two geopoints in group</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"two-geopoints-group\">\n" +
                "                    <my-group>\n" +
                "                        <location1 />\n" +
                "                    </my-group>\n" +
                "                    <location2 />\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/location2\" type=\"geopoint\" />\n" +
                "            <bind nodeset=\"/data/my-group/location1\" type=\"geopoint\" />\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <group ref=\"/data/my-group\">\n" +
                "            <input ref=\"/data/my-group/location1\"> <label>Location</label> </input>\n" +
                "        </group>\n" +
                "\n" +
                "        <input ref=\"/data/location2\"> <label>Location</label> </input>\n" +
                "    </h:body>\n" +
                "</h:html>";

        File temp = File.createTempFile("geopoints_group_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(submissionForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("Two geopoints in group"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("two-geopoints-group"));
        assertThat(metadataFromFormDefinition.get(FileUtils.GEOMETRY_XPATH), is("/data/my-group/location1"));
    }

    @Test public void getMetadataFromFormDefinition_withGeopointInRepeat_returnsFirstGeopointBasedOnBodyOrder() throws IOException {
        String submissionForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "    xmlns=\"http://www.w3.org/2002/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>Two geopoints repeat</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"two-geopoints-repeat\">\n" +
                "                    <my-repeat>\n" +
                "                        <location1 />\n" +
                "                    </my-repeat>\n" +
                "                    <location2 />\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/location2\" type=\"geopoint\" />\n" +
                "            <bind nodeset=\"/data/my-repeat/location1\" type=\"geopoint\" />\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <repeat nodeset=\"/data/my-repeat\">\n" +
                "            <input ref=\"/data/my-repeat/location1\"> <label>Location</label> </input>\n" +
                "        </repeat>\n" +
                "        <input ref=\"/data/location2\"> <label>Location</label> </input>\n" +
                "    </h:body>\n" +
                "</h:html>";

        File temp = File.createTempFile("geopoints_repeat_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(submissionForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("Two geopoints repeat"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("two-geopoints-repeat"));
        assertThat(metadataFromFormDefinition.get(FileUtils.GEOMETRY_XPATH), is("/data/location2"));
    }

    @Test public void getMetadataFromFormDefinition_withSetGeopointBeforeBodyGeopoint_returnsFirstGeopointInInstance() throws IOException {
        String submissionForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "    xmlns:odk=\"http://www.opendatakit.org/xforms\"\n" +
                "    xmlns=\"http://www.w3.org/2002/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>Setgeopoint before</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"set-geopoint-before\">\n" +
                "                    <location1 />\n" +
                "                    <location2 />\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/location2\" type=\"geopoint\" />\n" +
                "            <bind nodeset=\"/data/location1\" type=\"geopoint\" />\n" +
                "            <odk:setgeopoint ref=\"/data/location1\" event=\"odk-instance-first-load\"/>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <input ref=\"/data/location2\"> <label>Location</label> </input>\n" +
                "    </h:body>\n" +
                "</h:html>";

        File temp = File.createTempFile("geopoints_repeat_form", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(submissionForm);
        out.close();

        HashMap<String, String> metadataFromFormDefinition = FileUtils.getMetadataFromFormDefinition(temp);

        assertThat(metadataFromFormDefinition.get(FileUtils.TITLE), is("Setgeopoint before"));
        assertThat(metadataFromFormDefinition.get(FileUtils.FORMID), is("set-geopoint-before"));
        assertThat(metadataFromFormDefinition.get(FileUtils.GEOMETRY_XPATH), is("/data/location1"));
    }

    @Test public void whenFormVersionIsEmpty_shouldBeTreatedAsNull() throws IOException {
        String simpleForm = "<?xml version=\"1.0\"?>\n" +
                "<h:html xmlns=\"http://www.w3.org/2002/xforms\"\n" +
                "        xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "        xmlns:orx=\"http://openrosa.org/xforms\">\n" +
                "    <h:head>\n" +
                "        <h:title>My Survey</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"mysurvey\" orx:version=\"   \">\n" +
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
        assertThat(metadataFromFormDefinition.get(FileUtils.VERSION), is(nullValue()));
    }

    @Test
    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    public void simplifyScopedStoragePathTest() {
        assertThat(FileUtils.simplifyScopedStoragePath(null), is(nullValue()));
        assertThat(FileUtils.simplifyScopedStoragePath(""), is(""));
        assertThat(FileUtils.simplifyScopedStoragePath("blahblahblah"), is("blahblahblah"));
        assertThat(FileUtils.simplifyScopedStoragePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers"), is("/sdcard/Android/data/org.odk.collect.android/files/layers"));
        assertThat(FileUtils.simplifyScopedStoragePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"), is("/sdcard/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"));
    }

    @Test
    public void whenTryToListFilesOnNullFile_shouldReturnEmptyArray() {
        assertThat(FileUtils.listFiles(null), Matchers.is(empty()));
    }

    @Test
    public void whenTryToListFilesOnFileThatDoesNotExist_shouldReturnEmptyArray() {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        assertThat(FileUtils.listFiles(file), Matchers.is(empty()));
    }
}
