package org.odk.collect.android.formmanagement;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.openrosa.api.ManifestFile;
import org.odk.collect.android.openrosa.api.MediaFile;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.FileUtils.getMd5Hash;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormListSynchronizerTest {

    private final List<FormListItem> formList = asList(
            new FormListItem("http://example.com/form-1", "form-1", "server", "md5:form-1-hash", "Form 1", null),
            new FormListItem("http://example.com/form-2", "form-2", "server", "md5:form-2-hash", "Form 2", "http://example.com/form-2-manifest")
    );

    private ServerFormListSynchronizer synchronizer;
    private RecordingMultiFormDownloader formDownloader;
    private FormRepository formRepository;
    private MediaFileRepository mediaFileRepository;

    @Before
    public void setup() throws Exception {
        formRepository = new InMemFormRepository();
        mediaFileRepository = mock(MediaFileRepository.class);

        FormListApi formListAPI = mock(FormListApi.class);
        when(formListAPI.fetchFormList()).thenReturn(formList);

        when(formListAPI.fetchManifest(formList.get(1).getManifestURL())).thenReturn(new ManifestFile("manifest-2-hash", asList(
                new MediaFile("blah.txt", "md5:" + getMd5Hash(new ByteArrayInputStream("blah".getBytes())), "http://example.com/media-file")))
        );

        formDownloader = new RecordingMultiFormDownloader();
        synchronizer = new ServerFormListSynchronizer(formRepository, mediaFileRepository, formListAPI, formDownloader);
    }

    @Test
    public void whenNoFormsExist_downloadsAndSavesAllFormsInList() throws Exception {
        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1", "form-2"));
    }

    @Test
    public void whenAFormExists_deletesFormsNotInList() throws Exception {
        formRepository.save(new Form.Builder()
                .id(3L)
                .jrFormId("form-3")
                .md5Hash("form-3-hash")
                .build());

        synchronizer.synchronize();
        assertThat(formRepository.contains("form-3"), is(false));
    }

    @Test
    public void whenAFormExists_andListContainsUpdatedVersion_replacesFormWithListVersion() throws Exception {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .md5Hash("form-2-hash-old")
                .build());

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1", "form-2"));
    }

    @Test
    public void whenAFormExists_andHasNewMediaFileOnServer_replacesFormWithListVersion() throws Exception {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .jrVersion("server")
                .md5Hash("form-2-hash")
                .build());
        when(mediaFileRepository.getAll("form-2", "server")).thenReturn(emptyList());

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1", "form-2"));
    }

    @Test
    public void whenAFormExists_andHasUpdatedMediaFileOnServer_replacesFormWithListVersion() throws Exception {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .jrVersion("server")
                .md5Hash("form-2-hash")
                .build());

        File oldMediaFile = File.createTempFile("blah", ".csv");
        writeToFile(oldMediaFile, "blah before");
        when(mediaFileRepository.getAll("form-2", "server")).thenReturn(asList(oldMediaFile));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1", "form-2"));
    }

    @Test
    public void whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_doesNotDownload() throws Exception {
        formRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("form-1")
                .jrVersion("server")
                .md5Hash("form-1-hash")
                .build());

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-2"));
    }

    @Test
    public void whenFormExists_doesNotDownload() throws Exception {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .jrVersion("server")
                .md5Hash("form-2-hash")
                .build());

        File mediaFile = File.createTempFile("blah", ".csv");
        writeToFile(mediaFile, "blah");
        when(mediaFileRepository.getAll("form-2", "server")).thenReturn(asList(mediaFile));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1"));
    }

    private void writeToFile(File mediaFile, String blah) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(mediaFile));
        bw.write(blah);
        bw.close();
    }

    private static class InMemFormRepository implements FormRepository {

        private final List<Form> forms = new ArrayList<>();

        @Override
        public void save(Form form) {
            forms.add(form);
        }

        @Override
        public boolean contains(String jrFormID) {
            return forms.stream().anyMatch(form -> form.getJrFormId().equals(jrFormID));
        }

        @Override
        public List<Form> getAll() {
            return new ArrayList<>(forms); // Avoid anything  mutating the list externally
        }

        @Nullable
        @Override
        public Form getByMd5Hash(String hash) {
            return forms.stream().filter(form -> form.getMD5Hash().equals(hash)).findFirst().orElse(null);
        }

        @Override
        public void delete(Long id) {
            forms.removeIf(form -> form.getId().equals(id));
        }
    }

    private static class RecordingMultiFormDownloader implements FormDownloader {

        private final List<String> formsDownloaded = new ArrayList<>();

        @Override
        public void downloadForm(ServerFormDetails form) {
            formsDownloaded.add(form.getFormId());
        }

        public List<String> getDownloadedForms() {
            return formsDownloaded;
        }
    }
}