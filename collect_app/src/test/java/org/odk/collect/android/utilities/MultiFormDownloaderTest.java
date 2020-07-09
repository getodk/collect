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
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.openrosa.OpenRosaXmlFetcher;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MultiFormDownloaderTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    OpenRosaXmlFetcher openRosaXMLFetcher;

    @Before
    public void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
               @Override
               public OpenRosaXmlFetcher provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
                   return openRosaXMLFetcher;
               }
        });
    }

    /**
     * Verifies that a form without media can successfully go through the download process. Regression
     * test for https://github.com/getodk/collect/issues/3535.
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

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails serverFormDetails = new ServerFormDetails("No media", "https://testserver/no-media.xml",
                null, "basic", "2019121201",
                "hash", null, false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(serverFormDetails.getFormName(), serverFormDetails.getDownloadUrl(), null);
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(serverFormDetails);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
        assertThat(messages.get(serverFormDetails), is("Success"));
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

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails serverFormDetails = new ServerFormDetails("Media", "https://testserver/media.xml",
                "https://testserver/media-manifest.xml", "media", "2019121201",
                "hash", "manifestHash", false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(serverFormDetails.getFormName(), serverFormDetails.getDownloadUrl(), null);
        doReturn("").when(downloader).downloadManifestAndMediaFiles(any(), any(), any(), anyInt(), anyInt(), any());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(serverFormDetails);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
        assertThat(messages.get(serverFormDetails), is("Success"));
    }

    /**
     * Forms with references to external secondary instance need to have the secondary instance
     * available at time of form parse.
     *
     * See https://github.com/getodk/collect/issues/3635
     */
    @Test
    public void downloadingFormWithXmlExternalSecondaryInstance_Succeeds() throws Exception {
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

        when(openRosaXMLFetcher.getXML("https://testserver/manifest.xml")).thenReturn(buildManifestFetchResult("external-data.xml"));
        when(openRosaXMLFetcher.getFile("https://testserver/external-data.xml",
                null)).thenReturn(buildXmlExternalInstanceFetchResult());

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails test1 = new ServerFormDetails("basic-external-xml-instance", "https://testserver/form.xml",
                "https://testserver/manifest.xml", "basic-external-xml-instance", "20200101",
                "hash", "manifestHash", false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl(), null);
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
        assertThat(messages.get(test1), is("Success"));
    }

    @Test
    public void downloadingFormWithCsvExternalSecondaryInstance_Succeeds() throws Exception {
        String basicLastSaved = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" >\n" +
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
        File formXml = File.createTempFile("basicExternalCsvInstance", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicLastSaved);
        out.close();

        when(openRosaXMLFetcher.getXML("https://testserver/manifest.xml")).thenReturn(buildManifestFetchResult("external-data.csv"));
        when(openRosaXMLFetcher.getFile("https://testserver/external-data.csv",
                null)).thenReturn(buildCsvExternalInstanceFetchResult());

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails test1 = new ServerFormDetails("basic-external-csv-instance", "https://testserver/form.xml",
                "https://testserver/manifest.xml", "basic-external-csv-instance", "20200101",
                "hash", "manifestHash", false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl(), null);
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
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

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails test1 = new ServerFormDetails("Last Saved", "https://testserver/media.xml",
                "https://testserver/media-manifest.xml", "basic-last-saved", "20200101",
                "hash", "manifestHash", false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl(), null);
        doReturn("").when(downloader).downloadManifestAndMediaFiles(any(), any(), any(), anyInt(), anyInt(), any());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
        assertThat(messages.get(test1), is("Success"));
    }

    /**
     * Edge case: a form could have an attachment with filename last-saved.xml. This will get
     * replaced immediately on download and this test documents that behavior. We could let it go
     * through but let's replace it immediately to help a user who tries this troubleshoot.
     * Otherwise it would only be replaced when an instance is saved so a user could think everything
     * is ok if they only try launching the form once.
     *
     * This is an unfortunate side effect of using the form media folder to store the contents that
     * jr://instance/last-saved resolves to.
     *
     * Additionally, immediately replacing a secondary instance with name last-saved.xml avoid users
     * exploiting this current implementation quirk as a feature to preload defaults for the first
     * instance.
     * */
    @Test
    public void downloadingFormWithExternalSecondaryInstanceNamedLastSavedXml_Succeeds() throws Exception {
        String basicLastSaved = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" >\n" +
                "    <h:head>\n" +
                "        <h:title>last-saved-attached</h:title>\n" +
                "        <model>\n" +
                "            <instance>\n" +
                "                <data id=\"last-saved-attached\">\n" +
                "                    <first/>\n" +
                "                </data>\n" +
                "            </instance>\n" +
                "            <instance id=\"external-xml\" src=\"jr://file/last-saved.xml\" />\n" +
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
        File formXml = File.createTempFile("lastSavedAttached", ".xml");
        formXml.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(formXml));
        out.write(basicLastSaved);
        out.close();

        when(openRosaXMLFetcher.getXML("https://testserver/manifest.xml")).thenReturn(buildManifestFetchResult("last-saved.xml"));
        when(openRosaXMLFetcher.getFile("https://testserver/last-saved.xml",
                null)).thenReturn(buildXmlExternalInstanceFetchResult());

        MultiFormDownloader downloader = spy(new MultiFormDownloader());
        ServerFormDetails serverFormDetails = new ServerFormDetails("last-saved-attached", "https://testserver/form.xml",
                "https://testserver/manifest.xml", "last-saved-attached", "20200101",
                "hash", "manifestHash", false, false);
        MultiFormDownloader.FileResult result = new MultiFormDownloader.FileResult(formXml, true);
        doReturn(result).when(downloader).downloadXform(serverFormDetails.getFormName(), serverFormDetails.getDownloadUrl(), null);

        List<ServerFormDetails> forms = new ArrayList<>();
        forms.add(serverFormDetails);

        HashMap<ServerFormDetails, String> messages = downloader.downloadForms(forms, null);
        assertThat(messages.get(serverFormDetails), containsString("<label> node for itemset doesn't exist!"));
    }

    public static DocumentFetchResult buildManifestFetchResult(String filename) throws Exception {
        String manifest = "<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">\n" +
                " <mediaFile>\n" +
                "  <filename>" + filename + "</filename>\n" +
                "  <hash>hash</hash>\n" +
                "  <downloadUrl>https://testserver/" + filename + "</downloadUrl>\n" +
                " </mediaFile>\n" +
                "</manifest>";
        Document doc = new Document();
        KXmlParser parser = new KXmlParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new InputStreamReader(new ByteArrayInputStream(manifest.getBytes())));
        doc.parse(parser);
        return new DocumentFetchResult(doc, true, "hash");
    }

    private static InputStream buildXmlExternalInstanceFetchResult() {
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

    private static InputStream buildCsvExternalInstanceFetchResult() {
        String externalInstance = "label,name\n" +
                "A, a\n" +
                "B, b\n" +
                "C, c\n";
        return new ByteArrayInputStream(externalInstance.getBytes());
    }
}