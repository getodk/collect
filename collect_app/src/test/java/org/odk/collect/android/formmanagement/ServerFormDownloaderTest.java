package org.odk.collect.android.formmanagement;

import com.google.common.io.Files;

import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormListItem;
import org.odk.collect.android.forms.FormSource;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.forms.MediaFile;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formSource, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));
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

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formSource.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream("contents1".getBytes()));
        when(formSource.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream("contents2".getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formSource, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));
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

    /**
     * Form parsing might need access to media files (external secondary instances) for example
     * so we need to make sure we've got those files in the right place before we parse.
     */
    @Test
    public void whenFormHasMediaFiles_downloadsAndSavesFormAndMediaFiles_beforeParsingForm() throws Exception {
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

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formSource.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream("contents1".getBytes()));
        when(formSource.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream("contents2".getBytes()));

        FormMetadataParser formMetadataParser = new FormMetadataParser(ReferenceManager.instance()) {
            @Override
            public Map<String, String> parse(File file, File mediaDir) {
                File[] mediaFiles = mediaDir.listFiles();
                assertThat(mediaFiles.length, is(2));
                assertThat(stream(mediaFiles).map(File::getName).collect(toList()), containsInAnyOrder("file1", "file2"));

                return super.parse(file, mediaDir);
            }
        };

        ServerFormDownloader downloader = new ServerFormDownloader(formSource, formsRepository, cacheDir, formsDir.getAbsolutePath(), formMetadataParser);
        downloader.downloadForm(serverFormDetails, null, null);
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

        CancelAfterFormDownloadFormSource formListApi = new CancelAfterFormDownloadFormSource(xform);
        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));

        try {
            downloader.downloadForm(serverFormDetails, null, formListApi);
            fail("Expected exception");
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

        CancelAfterMediaFileDownloadFormSource formListApi = new CancelAfterMediaFileDownloadFormSource(xform);
        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));

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
    public void beforeDownloadingEachMediaFile_reportsProgress() throws Exception {
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

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formSource.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream("contents".getBytes()));
        when(formSource.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream("contents".getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formSource, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));
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

        FormSource formSource = mock(FormSource.class);
        when(formSource.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formSource, formsRepository, cacheDir, formsDir.getAbsolutePath(), new FormMetadataParser(ReferenceManager.instance()));
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

    public static class CancelAfterFormDownloadFormSource implements FormSource, Supplier<Boolean> {

        private final String xform;
        private boolean isCancelled;

        public CancelAfterFormDownloadFormSource(String xform) {
            this.xform = xform;
        }

        @Override
        public InputStream fetchForm(String formURL) throws FormSourceException {
            isCancelled = true;
            return new ByteArrayInputStream(xform.getBytes());
        }

        @Override
        public List<FormListItem> fetchFormList() throws FormSourceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ManifestFile fetchManifest(String manifestURL) throws FormSourceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException {
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

    public static class CancelAfterMediaFileDownloadFormSource implements FormSource, Supplier<Boolean> {

        private final String xform;
        private boolean isCancelled;

        public CancelAfterMediaFileDownloadFormSource(String xform) {
            this.xform = xform;
        }

        @Override
        public InputStream fetchForm(String formURL) throws FormSourceException {
            return new ByteArrayInputStream(xform.getBytes());
        }

        @Override
        public InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException {
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
        public List<FormListItem> fetchFormList() throws FormSourceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ManifestFile fetchManifest(String manifestURL) throws FormSourceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean get() {
            return isCancelled;
        }
    }
}