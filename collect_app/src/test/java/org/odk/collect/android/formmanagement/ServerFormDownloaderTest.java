package org.odk.collect.android.formmanagement;

import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.server.FormApiException;
import org.odk.collect.server.FormListApi;
import org.odk.collect.server.FormListItem;
import org.odk.collect.server.ManifestFile;
import org.odk.collect.server.MediaFile;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.FormUtils.buildForm;
import static org.odk.collect.android.support.FormUtils.createXForm;
import static org.odk.collect.android.utilities.FileUtils.read;
import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormDownloaderTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();

    private File cacheDir;
    private File formsDir;

    @Before
    public void setup() {
        cacheDir = Files.createTempDir();
        formsDir = Files.createTempDir();
    }

    @Test
    public void downloadsAndSavesForm() throws Exception {
        String xform = createXForm("id", "version");
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                "Form",
                "http://downloadUrl",
                "http://manifestUrl",
                "id",
                "version",
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                null);

        FormListApi formListApi = mock(FormListApi.class);
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());
        downloader.downloadForm(serverFormDetails, null, null);

        List<Form> allForms = formsRepository.getAll();
        assertThat(allForms.size(), is(1));
        Form form = allForms.get(0);
        assertThat(form.getJrFormId(), is("id"));

        File formFile = new File(getAbsoluteFilePath(formsDir.getAbsolutePath(), form.getFormFilePath()));
        assertThat(formFile.exists(), is(true));
        assertThat(new String(read(formFile)), is(xform));
    }

    @Test
    public void whenFormHasMediaFiles_downloadsAndSavesFormAndMediaFiles() throws Exception {
        String xform = createXForm("id", "version");
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                "Form",
                "http://downloadUrl",
                "http://manifestUrl",
                "id",
                "version",
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                new ManifestFile("", asList(
                        new MediaFile("file1", "hash-1", "http://file1"),
                        new MediaFile("file2", "hash-2", "http://file2")
                )));

        FormListApi formListApi = mock(FormListApi.class);
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formListApi.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream("contents1".getBytes()));
        when(formListApi.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream("contents2".getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());
        downloader.downloadForm(serverFormDetails, null, null);

        List<Form> allForms = formsRepository.getAll();
        assertThat(allForms.size(), is(1));
        Form form = allForms.get(0);
        assertThat(form.getJrFormId(), is("id"));

        File formFile = new File(getAbsoluteFilePath(formsDir.getAbsolutePath(), form.getFormFilePath()));
        assertThat(formFile.exists(), is(true));
        assertThat(new String(read(formFile)), is(xform));

        File mediaFile1 = new File(form.getFormMediaPath() + "/file1");
        assertThat(mediaFile1.exists(), is(true));
        assertThat(new String(read(mediaFile1)), is("contents1"));

        File mediaFile2 = new File(form.getFormMediaPath() + "/file2");
        assertThat(mediaFile2.exists(), is(true));
        assertThat(new String(read(mediaFile2)), is("contents2"));
    }

    @Test
    public void afterDownloadingXForm_cancelling_throwsInterruptedExceptionAndDoesNotSaveAnything() throws Exception {
        String xform = createXForm("id", "version");
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                "Form",
                "http://downloadUrl",
                "http://manifestUrl",
                "id",
                "version",
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                null);

        CancelAfterFormDownloadFormListApi formListApi = new CancelAfterFormDownloadFormListApi(xform);
        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());

        try {
            downloader.downloadForm(serverFormDetails, null, formListApi);
            fail("Excepted exception");
        } catch (InterruptedException e) {
            assertThat(formsRepository.getAll(), is(empty()));
            assertThat(asList(new File(getCacheFilesPath()).listFiles()), is(empty()));
            assertThat(asList(new File(getFormFilesPath()).listFiles()), is(empty()));
        }
    }

    @Test
    public void afterDownloadingMediaFile_cancelling_throwsInterruptedExceptionAndDoesNotSaveAnything() throws Exception {
        String xform = createXForm("id", "version");
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                "Form",
                "http://downloadUrl",
                "http://manifestUrl",
                "id",
                "version",
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                new ManifestFile("", asList(
                        new MediaFile("file1", "hash-1", "http://file1"),
                        new MediaFile("file2", "hash-2", "http://file2")
                )));

        CancelAfterMediaFileDownloadFormListApi formListApi = new CancelAfterMediaFileDownloadFormListApi(xform);
        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());

        try {
            downloader.downloadForm(serverFormDetails, null, formListApi);
            fail("Excepted exception");
        } catch (InterruptedException e) {
            assertThat(formsRepository.getAll(), is(empty()));
            assertThat(asList(new File(getCacheFilesPath()).listFiles()), is(empty()));

            // The media directory is created early for some reason
            assertThat(asList(new File(getFormFilesPath()).listFiles()), contains(new File(getFormFilesPath() + "/Form-media")));
        }
    }

    @Test
    public void beforeDownloadingMediaFile_reportsProgress() throws Exception {
        String xform = createXForm("id", "version");
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                "Form",
                "http://downloadUrl",
                "http://manifestUrl",
                "id",
                "version",
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                new ManifestFile("", asList(
                    new MediaFile("file1", "hash-1", "http://file1"),
                    new MediaFile("file2", "hash-2", "http://file2")
                )));

        FormListApi formListApi = mock(FormListApi.class);
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formListApi.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream("contents".getBytes()));
        when(formListApi.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream("contents".getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());
        RecordingProgressReporter progressReporter = new RecordingProgressReporter();
        downloader.downloadForm(serverFormDetails, progressReporter, null);

        assertThat(progressReporter.reports, contains(1, 2));
    }

    @Test
    public void whenFormIsSoftDeleted_unDeletesForm() throws Exception {
        Form form = buildForm(1L, "deleted-form", "version", getFormFilesPath())
                .deleted(true)
                .build();
        formsRepository.save(form);

        String xform = createXForm(form.getJrFormId(), form.getJrVersion());
        ServerFormDetails serverFormDetails = new ServerFormDetails(
                form.getDisplayName(),
                "http://downloadUrl",
                null,
                form.getJrFormId(),
                form.getJrVersion(),
                "md5:" + FileUtils.getMd5Hash(new ByteArrayInputStream(xform.getBytes())),
                true,
                false,
                null);

        FormListApi formListApi = mock(FormListApi.class);
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath());
        downloader.downloadForm(serverFormDetails, null, null);
        assertThat(formsRepository.get(1L).isDeleted(), is(false));
    }

    private String getFormFilesPath() {
        return formsDir.getAbsolutePath();
    }

    private String getCacheFilesPath() {
        return cacheDir.getAbsolutePath();
    }

    public static class RecordingProgressReporter implements FormDownloader.ProgressReporter {

        List<Integer> reports = new ArrayList<>();

        @Override
        public void onDownloadingMediaFile(int count) {
            reports.add(count);
        }
    }

    public static class CancelAfterFormDownloadFormListApi implements FormListApi, Supplier<Boolean> {

        private final String xform;
        private boolean isCancelled;

        public CancelAfterFormDownloadFormListApi(String xform) {
            this.xform = xform;
        }

        @Override
        public InputStream fetchForm(String formURL) throws FormApiException {
            isCancelled = true;
            return new ByteArrayInputStream(xform.getBytes());
        }

        @Override
        public List<FormListItem> fetchFormList() throws FormApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ManifestFile fetchManifest(String manifestURL) throws FormApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream fetchMediaFile(String mediaFileURL) throws FormApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateUrl(String url) {

        }

        @Override
        public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {

        }

        @Override
        public Boolean get() {
            return isCancelled;
        }
    }

    public static class CancelAfterMediaFileDownloadFormListApi implements FormListApi, Supplier<Boolean> {

        private final String xform;
        private boolean isCancelled;

        public CancelAfterMediaFileDownloadFormListApi(String xform) {
            this.xform = xform;
        }

        @Override
        public InputStream fetchForm(String formURL) throws FormApiException {
            return new ByteArrayInputStream(xform.getBytes());
        }

        @Override
        public InputStream fetchMediaFile(String mediaFileURL) throws FormApiException {
            isCancelled = true;
            return new ByteArrayInputStream("contents".getBytes());
        }

        @Override
        public void updateUrl(String url) {

        }

        @Override
        public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {

        }

        @Override
        public List<FormListItem> fetchFormList() throws FormApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ManifestFile fetchManifest(String manifestURL) throws FormApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean get() {
            return isCancelled;
        }
    }
}