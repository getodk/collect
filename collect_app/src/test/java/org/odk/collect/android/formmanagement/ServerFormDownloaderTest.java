package org.odk.collect.android.formmanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.ManifestFile;
import org.odk.collect.android.openrosa.api.MediaFile;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.FormUtils.buildForm;
import static org.odk.collect.android.support.FormUtils.createXForm;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormDownloaderTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();
    private final FormListApi formListApi = mock(FormListApi.class);

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
        storagePathProvider = new StoragePathProvider();
        new StorageInitializer().createOdkDirsOnStorage();
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
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formListApi.fetchMediaFile("http://file1")).thenReturn(new ByteArrayInputStream(xform.getBytes()));
        when(formListApi.fetchMediaFile("http://file2")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository);
        RecordingProgressReporter progressReporter = new RecordingProgressReporter();
        downloader.downloadForm(serverFormDetails, progressReporter);

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
        when(formListApi.fetchForm("http://downloadUrl")).thenReturn(new ByteArrayInputStream(xform.getBytes()));

        MultiFormDownloader multiFormDownloader = mock(MultiFormDownloader.class);
        HashMap<ServerFormDetails, String> results = new HashMap<ServerFormDetails, String>() {{
            put(serverFormDetails, Collect.getInstance().getString(R.string.success));
        }};
        when(multiFormDownloader.downloadForms(any(), any())).thenReturn(results);

        ServerFormDownloader downloader = new ServerFormDownloader(formListApi, formsRepository);
        downloader.downloadForm(serverFormDetails, null);
        assertThat(formsRepository.get(1L).isDeleted(), is(false));
    }

    private String getFormFilesPath() {
        return storagePathProvider.getDirPath(StorageSubdirectory.FORMS);
    }

    public static class RecordingProgressReporter implements FormDownloader.ProgressReporter {

        List<Integer> reports = new ArrayList<>();

        @Override
        public void onDownloadingMediaFile(int count) {
            reports.add(count);
        }
    }
}