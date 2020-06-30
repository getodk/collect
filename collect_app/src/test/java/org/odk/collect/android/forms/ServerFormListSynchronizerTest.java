package org.odk.collect.android.forms;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.logic.ManifestFile;
import org.odk.collect.android.logic.MediaFile;
import org.odk.collect.android.openrosa.api.FormAPI;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.utilities.FormDownloader;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.FormDetails.toFormDetails;
import static org.odk.collect.android.utilities.FileUtils.getMd5Hash;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormListSynchronizerTest {

    private final List<FormListItem> formList = asList(
            new FormListItem("http://example.com/form-1", "form-1", "server", "md5:form-1-hash", "Form 1", null),
            new FormListItem("http://example.com/form-2", "form-2", "server", "md5:form-2-hash", "Form 2", "http://example.com/form-2-manifest")
    );

    private ServerFormListSynchronizer synchronizer;
    private FormDownloader formDownloader;
    private FormRepository formRepository;
    private MediaFileRepository mediaFileRepository;

    @Before
    public void setup() throws Exception {
        formRepository = new InMemFormRepository();
        mediaFileRepository = mock(MediaFileRepository.class);

        FormAPI formAPI = mock(FormAPI.class);
        when(formAPI.fetchFormList()).thenReturn(formList);

        when(formAPI.fetchManifest(formList.get(1).getManifestURL())).thenReturn(new ManifestFile("manifest-2-hash", asList(
                new MediaFile("blah.txt", getMd5Hash(new ByteArrayInputStream("blah".getBytes())), "http://example.com/media-file")))
        );

        formDownloader = mock(FormDownloader.class);

        synchronizer = new ServerFormListSynchronizer(formRepository, mediaFileRepository, formAPI, formDownloader);
    }

    @Test
    public void whenNoFormsExist_downloadsAndSavesAllFormsInList() {
        synchronizer.synchronize();
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(0)))), any());
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
    }

    @Test
    public void whenAFormExists_deletesFormsNotInList() {
        formRepository.save(new Form.Builder()
                .id(3L)
                .jrFormId("form-3")
                .md5Hash("form-3-hash")
                .build());

        synchronizer.synchronize();
        assertThat(formRepository.contains("form-3"), is(false));
    }

    @Test
    public void whenAFormExists_andListContainsUpdatedVersion_replacesFormWithListVersion() {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .md5Hash("form-2-hash-old")
                .build());

        synchronizer.synchronize();
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
    }

    @Test
    public void whenAFormExists_andHasNewMediaFileOnServer_replacesFormWithListVersion() {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .jrVersion("server")
                .md5Hash("form-2-hash")
                .build());
        when(mediaFileRepository.getAll("form-2", "server")).thenReturn(emptyList());

        synchronizer.synchronize();
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
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
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
    }

    @Test
    public void whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_doesNotDownload() {
        formRepository.save(new Form.Builder()
                .id(1L)
                .jrFormId("form-1")
                .jrVersion("server")
                .md5Hash("form-1-hash")
                .build());

        synchronizer.synchronize();
        verify(formDownloader, never()).downloadForms(eq(asList(toFormDetails(formList.get(0)))), any());
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
        verify(formDownloader, never()).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
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

        @Override
        public void delete(Long id) {
            forms.removeIf(form -> form.getId().equals(id));
        }
    }
}