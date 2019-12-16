package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.logic.FormDetails;
import org.robolectric.RobolectricTestRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class FormDownloaderTest {
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
        File temp = File.createTempFile("basicNoMedia", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(basicNoMedia);
        out.close();

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails test1 = new FormDetails("No media", "https://testserver/no-media.xml",
                null, "test1", "2019121201",
                "hash", null, false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(temp, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(test1), is("Success"));
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
        File temp = File.createTempFile("basicMedia", ".xml");
        temp.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(basicMedia);
        out.close();

        FormDownloader downloader = spy(new FormDownloader());
        FormDetails test1 = new FormDetails("Media", "https://testserver/media.xml",
                "https://testserver/media-manifest.xml", "media", "2019121201",
                "hash", "manifestHash", false, false);
        FormDownloader.FileResult result = new FormDownloader.FileResult(temp, true);
        doReturn(result).when(downloader).downloadXform(test1.getFormName(), test1.getDownloadUrl());
        doReturn("").when(downloader).downloadManifestAndMediaFiles(any(), any(), any(), anyInt(), anyInt());
        doReturn(true).when(downloader).installEverything(any(), any(), any());

        List<FormDetails> forms = new ArrayList<>();
        forms.add(test1);

        HashMap<FormDetails, String> messages = downloader.downloadForms(forms);
        assertThat(messages.get(test1), is("Success"));
    }
}