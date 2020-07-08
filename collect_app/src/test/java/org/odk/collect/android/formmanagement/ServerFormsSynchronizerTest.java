package org.odk.collect.android.formmanagement;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormsSynchronizerTest {

    private ServerFormsSynchronizer synchronizer;
    private RecordingFormDownloader formDownloader;
    private FormRepository formRepository;
    private ServerFormsDetailsFetcher serverFormDetailsFetcher;

    @Before
    public void setup() {
        formRepository = new InMemFormRepository();
        formDownloader = new RecordingFormDownloader();
        serverFormDetailsFetcher = mock(ServerFormsDetailsFetcher.class);

        synchronizer = new ServerFormsSynchronizer(serverFormDetailsFetcher, formRepository, formDownloader);
    }

    @Test
    public void downloadsNewForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, true, false)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1"));
    }

    @Test
    public void downloadsUpdatedForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, false, true)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1"));
    }

    @Test
    public void deletesFormsNotInList() throws Exception {
        formRepository.save(new Form.Builder()
                .id(3L)
                .jrFormId("form-3")
                .md5Hash("form-3-hash")
                .build());

        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, false, false)
        ));

        synchronizer.synchronize();
        assertThat(formRepository.contains("form-3"), is(false));
    }

    @Test
    public void doesNotDownloadExistingForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, false, false)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), is(empty()));
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

    private static class RecordingFormDownloader implements FormDownloader {

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