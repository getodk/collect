package org.odk.collect.android.formmanagement;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.formstest.InMemFormsRepository;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.ManifestFile;
import org.odk.collect.forms.MediaFile;
import org.odk.collect.formstest.FormUtils;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.shared.TempFiles;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormsDetailsFetcherTest {

    private final List<FormListItem> formList = asList(
            new FormListItem("http://example.com/form-1", "form-1", "1", "md5:form-1-hash", "Form 1", null),
            new FormListItem("http://example.com/form-2", "form-2", "2", "md5:form-2-hash", "Form 2", "http://example.com/form-2-manifest"),
            new FormListItem("http://example.com/form-3", "form-3", "1", null, "Form 1", "http://example.com/form-3-manifest")
    );

    private static final String FILE_CONTENT = "blah";
    private final MediaFile mediaFile = new MediaFile("blah.txt", "md5:" + Md5.getMd5Hash(new ByteArrayInputStream(FILE_CONTENT.getBytes())), "http://example.com/media-file");

    private ServerFormsDetailsFetcher fetcher;
    private FormsRepository formsRepository;

    @Before
    public void setup() throws Exception {
        formsRepository = new InMemFormsRepository();

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchFormList()).thenReturn(formList);

        when(formSource.fetchManifest(formList.get(1).getManifestURL())).thenReturn(new ManifestFile("manifest-2-hash", asList(
                mediaFile))
        );

        when(formSource.fetchManifest(formList.get(2).getManifestURL())).thenReturn(new ManifestFile("manifest-3-hash", asList(
                mediaFile))
        );

        DiskFormsSynchronizer diskFormsSynchronizer = mock(DiskFormsSynchronizer.class);
        fetcher = new ServerFormsDetailsFetcher(formsRepository, formSource, diskFormsSynchronizer);
    }

    @Test
    public void whenFormHasManifestUrl_returnsMediaFilesInDetails() throws Exception {
        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();

        assertThat(getForm(serverFormDetails, "form-1").getManifest(), nullValue());
        assertThat(getForm(serverFormDetails, "form-2").getManifest().getMediaFiles(), contains(mediaFile));
    }

    @Test
    public void whenNoFormsExist_isNotOnDevice() throws Exception {
        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-1").isNotOnDevice(), is(true));
        assertThat(getForm(serverFormDetails, "form-2").isNotOnDevice(), is(true));
    }

    @Test
    public void whenAFormIsSoftDeleted_isNotOnDevice() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-1")
                .version("server")
                .md5Hash("form-1-hash")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("form-1", "server").getAbsolutePath())
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-1").isNotOnDevice(), is(true));
        assertThat(getForm(serverFormDetails, "form-2").isNotOnDevice(), is(true));
    }

    @Test
    public void whenAFormExists_andListContainsUpdatedVersion_isUpdated() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .md5Hash("form-2-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-2", null).getAbsolutePath())
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-2").isUpdated(), is(true));
    }

    @Test
    public void whenAFormExists_andHasNewMediaFileOnServer_isUpdated() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath("/does-not-exist")
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-2").isUpdated(), is(true));
    }

    @Test
    public void whenAFormExists_andHasUpdatedMediaFileOnServer_isUpdated() throws Exception {
        File mediaDir = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath(mediaDir.getAbsolutePath())
                .build());

        File oldMediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv");
        writeToFile(oldMediaFile, "blah before");

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-2").isUpdated(), is(true));
    }

    @Test
    public void whenAFormExists_andItsNewerVersionWithUpdatedMediaFilesHasBeenAlreadyDownloaded_isNotNewOrUpdated() throws Exception {
        File mediaDir1 = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").getAbsolutePath())
                .formMediaPath(mediaDir1.getAbsolutePath())
                .build());

        File mediaFile1 = TempFiles.createTempFile(mediaDir1, mediaFile.getFilename());
        writeToFile(mediaFile1, "old blah");

        File mediaDir2 = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath(mediaDir2.getAbsolutePath())
                .build());

        File mediaFile2 = TempFiles.createTempFile(mediaDir2, mediaFile.getFilename());
        writeToFile(mediaFile2, FILE_CONTENT);

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-2");

        assertThat(form.isUpdated(), is(false));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenAFormExists_andItsNewerVersionIsAvailableButHasNullHash_isNotNewOrUpdated() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-3")
                .version("0")
                .md5Hash("form-3-hash")
                .formFilePath(FormUtils.createXFormFile("form-3", "1").getAbsolutePath())
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-3");

        assertThat(form.isUpdated(), is(false));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenAFormExists_andItsNewerVersionHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-1")
                .version("0")
                .md5Hash("form-1-hash0")
                .formFilePath(FormUtils.createXFormFile("form-1", "0").getAbsolutePath())
                .build());

        formsRepository.save(new Form.Builder()
                .formId("form-1")
                .version("1")
                .md5Hash("form-1-hash")
                .formFilePath(FormUtils.createXFormFile("form-1", "1").getAbsolutePath())
                .deleted(true)
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-1");

        assertThat(form.isUpdated(), is(true));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenAFormExists_andItsNewerVersionWithMediaFilesHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() throws Exception {
        File mediaDir1 = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").getAbsolutePath())
                .formMediaPath(mediaDir1.getAbsolutePath())
                .build());

        File mediaFile1 = TempFiles.createTempFile(mediaDir1, mediaFile.getFilename());
        writeToFile(mediaFile1, FILE_CONTENT);

        File mediaDir2 = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath(mediaDir2.getAbsolutePath())
                .deleted(true)
                .build());

        File mediaFile2 = TempFiles.createTempFile(mediaDir2, mediaFile.getFilename());
        writeToFile(mediaFile2, FILE_CONTENT);

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-2");

        assertThat(form.isUpdated(), is(true));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_isNotNewOrUpdated() throws Exception {
        formsRepository.save(new Form.Builder()
                .formId("form-1")
                .version("1")
                .md5Hash("form-1-hash")
                .formFilePath(FormUtils.createXFormFile("form-1", "1").getAbsolutePath())
                .build());

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-1");

        assertThat(form.isUpdated(), is(false));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenFormExists_isNotNewOrUpdated() throws Exception {
        File mediaDir = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath(mediaDir.getAbsolutePath())
                .build());

        File mediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv");
        writeToFile(mediaFile, FILE_CONTENT);

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        ServerFormDetails form = getForm(serverFormDetails, "form-2");

        assertThat(form.isUpdated(), is(false));
        assertThat(form.isNotOnDevice(), is(false));
    }

    @Test
    public void whenAFormExists_andIsUpdatedOnServer_andDoesNotHaveNewMedia_isUpdated() throws Exception {
        File mediaDir = TempFiles.createTempDir();

        formsRepository.save(new Form.Builder()
                .formId("form-2")
                .md5Hash("form-2-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").getAbsolutePath())
                .formMediaPath(mediaDir.getAbsolutePath())
                .build());

        File localMediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv");
        writeToFile(localMediaFile, FILE_CONTENT);

        List<ServerFormDetails> serverFormDetails = fetcher.fetchFormDetails();
        assertThat(getForm(serverFormDetails, "form-2").isUpdated(), is(true));
    }

    private void writeToFile(File mediaFile, String blah) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(mediaFile));
        bw.write(blah);
        bw.close();
    }

    private ServerFormDetails getForm(List<ServerFormDetails> serverFormDetails, String s2) {
        return serverFormDetails.stream().filter(s -> s.getFormId().equals(s2)).findAny().get();
    }
}
