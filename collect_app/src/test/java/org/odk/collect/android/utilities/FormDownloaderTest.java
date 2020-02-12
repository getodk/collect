package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.openrosa.OpenRosaAPIClient;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FormDownloaderTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    OpenRosaAPIClient openRosaAPIClient;

    @Before
    public void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
               @Override
               public OpenRosaAPIClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
                   return openRosaAPIClient;
               }
        });}

    /**
     * Verifies that a form without media can successfully go through the download process. Regression
     * test for https://github.com/opendatakit/collect/issues/3535.
     *
     * The focus of this test is the form parsing behavior triggered by a download and how it
     * relates to a media folder that may or may not have been created. The downloading of forms and
     * saving of parsed form  values are mocked (and those concerns should be separated).
     */
    @Test
    public void downloadingFormWithoutMedia_Succeeds() throws Exception {
        String basicNoMedia = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
                "    <h:head>\n" +
                "        <h:title>basic</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"basic\">\n" +
                "                    <q1/>\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/q1\" type=\"string\"/>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <input ref=\"/data/q1\">\n" +
                "            <label>Question</label>\n" +
                "        </input>\n" +
                "    </h:body>\n" +
                "</h:html>";
        File formXml = File.createTempFile("basicNoMedia", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicNoMedia);
        out.close();

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails formDetails = new FormDetails("No media", "https://testserver/no-media.xml",
                null, "basic", "2019121201",
                "hash", null, false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(formDetails.getFormName(), formDetails.getDownloadUrl());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(formDetails);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(formDetails), is("Success"));
    }

    /**
     * Companion to downloading form without media.
     *
     * The focus of this test is the form parsing behavior triggered by a download and how it
     * relates to a media folder that may or may not have been created. The form downloading, media
     * downloading and saving of parsed form values are mocked.
     *
     * Note: what's important in this test is that the manifestURL in the FormDetails object is set.
     * It doesn't really matter that the form definition uses media but that's included to better
     * match reality.
     */
    @Test
    public void downloadingFormWithMedia_Succeeds() throws Exception {
        String basicMedia = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
                "    <h:head>\n" +
                "        <h:title>basic-media</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"basic-media\">\n" +
                "                    <q1/>\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <bind nodeset=\"/data/q1\" type=\"string\"/>\n" +
                "            <itext> \n" +
                "                <translation default=\"true()\" lang=\"English\">\n" +
                "                    <text id=\"/data/q1:label\"><value form=\"image\">jr://images/b.jpg</value></text>\n" +
                "                </translation>\n" +
                "            </itext>\n" +
                "        </model>\n" +
                "    </h:head>\n" +
                "    <h:body>\n" +
                "        <input ref=\"/data/q1\">\n" +
                "            <label>Question</label>\n" +
                "        </input>\n" +
                "    </h:body>\n" +
                "</h:html>";
        File formXml = File.createTempFile("basicMedia", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicMedia);
        out.close();

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails formDetails = new FormDetails("Media", "https://testserver/media.xml",
                "https://testserver/media-manifest.xml", "media", "2019121201",
                "hash", "manifestHash", false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(formDetails.getFormName(), formDetails.getDownloadUrl());
        doReturn("").when(downloader).downloadManifestAndMediaFiles(any(), any(), any(), anyInt(), anyInt());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(formDetails);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(formDetails), is("Success"));
    }

    /**
     * Forms with references to external secondary instance need to have the secondary instance
     * available at time of form parse.
     *
     * See https://github.com/opendatakit/collect/issues/3635
     */
    @Test
    public void downloadingFormWithExternalSecondaryInstance_Succeeds() throws Exception {
        String basicLastSaved = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" >\n" +
                "    <h:head>\n" +
                "        <h:title>basic-external-xml-instance</h:title>\n" +
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
        File formXml = File.createTempFile("basicExternalXmlInstance", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicLastSaved);
        out.close();

        when(openRosaAPIClient.getXML("https://testserver/manifest.xml")).thenReturn(buildManifestFetchResult());
        when(openRosaAPIClient.getFile("https://testserver/external-data.xml",
                null)).thenReturn(buildExternalInstanceFetchResult());

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails test1 = new FormDetails("basic-external-xml-instance", "https://testserver/form.xml",
                "https://testserver/manifest.xml", "basic-external-xml-instance", "20200101",
                "hash", "manifestHash", false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(test1), is("Success"));
    }

    /**
     * Forms with last-saved references are a special case of external secondary instances because
     * last-saved doesn't come from the remote server but is generated locally.
     */
    @Test
    public void downloadingFormWithLastSavedReference_Succeeds() throws Exception {
        String basicLastSaved = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\">\n" +
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
        File formXml = File.createTempFile("basicLastSaved", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicLastSaved);
        out.close();

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails test1 = new FormDetails("Last Saved", "https://testserver/media.xml",
                "https://testserver/media-manifest.xml", "basic-last-saved", "20200101",
                "hash", "manifestHash", false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl());
        doReturn("").when(downloader).downloadManifestAndMediaFiles(any(), any(), any(), anyInt(), anyInt());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(test1), is("Success"));
    }

    public static DocumentFetchResult buildManifestFetchResult() throws Exception {
        String manifest = "<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">\n" +
                " <mediaFile>\n" +
                "  <filename>external-data.xml</filename>\n" +
                "  <hash>hash</hash>\n" +
                "  <downloadUrl>https://testserver/external-data.xml</downloadUrl>\n" +
                " </mediaFile>\n" +
                "</manifest>";
        org.kxml2.kdom.Document doc = new Document();
        KXmlParser parser = new KXmlParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new InputStreamReader(new ByteArrayInputStream(manifest.getBytes())));
        doc.parse(parser);
        return new DocumentFetchResult(doc, true, "hash");
    }

    public static InputStream buildExternalInstanceFetchResult() {
        String externalInstance = "<root>\n" +
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
        return new ByteArrayInputStream(externalInstance.getBytes());
    }
}