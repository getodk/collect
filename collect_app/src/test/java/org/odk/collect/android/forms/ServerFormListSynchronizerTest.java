package org.odk.collect.android.forms;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.openrosa.api.FormAPI;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.utilities.FormDownloader;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.FormDetails.toFormDetails;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormListSynchronizerTest {

    private final List<FormListItem> formList = asList(
            new FormListItem("http://example.com/form-1", "form-1", "server", "form-1-hash", "Form 1", "http://example.com/form-1-manifest"),
            new FormListItem("http://example.com/form-2", "form-2", "server", "form-2-hash", "Form 2", "http://example.com/form-2-manifest")
    );

    private ServerFormListSynchronizer synchronizer;
    private FormDownloader formDownloader;
    private FormRepository formRepository;

    @Before
    public void setup() throws Exception {
        formRepository = new InMemFormRepository();

        FormAPI formAPI = mock(FormAPI.class);
        when(formAPI.fetchFormList()).thenReturn(formList);

        formDownloader = mock(FormDownloader.class);

        synchronizer = new ServerFormListSynchronizer(formRepository, formAPI, formDownloader);
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
                .md5Hash("form-2-hash-changed")
                .jrVersion("device")
                .build());

        synchronizer.synchronize();
        verify(formDownloader).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
    }

    @Test
    public void whenFormExists_doesNotDownload() {
        formRepository.save(new Form.Builder()
                .id(2L)
                .jrFormId("form-2")
                .md5Hash("form-2-hash")
                .jrVersion("1")
                .build());

        synchronizer.synchronize();
        verify(formDownloader, never()).downloadForms(eq(asList(toFormDetails(formList.get(1)))), any());
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