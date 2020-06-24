package org.odk.collect.android.forms;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.FormDownloader;
import org.odk.collect.android.utilities.FormListDownloader;

import java.util.ArrayList;
import java.util.HashMap;
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

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormListSynchronizerTest {

    private final List<FormDetails> formList = asList(
            new FormDetails(null, "http://example.com/form-1", null, "form-1", "server", "form-1-hash", null, false, false),
            new FormDetails(null, "http://example.com/form-2", null, "form-2", "server", "form-2-hash", null, false, false)
    );

    private ServerFormListSynchronizer synchronizer;
    private FormDownloader formDownloader;
    private FormRepository formRepository;

    @Before
    public void setup() {
        formRepository = new InMemFormRepository();

        FormListDownloader formListDownloader = mock(FormListDownloader.class);
        HashMap<String, FormDetails> formListResponse = new HashMap<>();
        for (FormDetails formDetails : formList) {
            formListResponse.put(formDetails.getFormId(), formDetails);
        }
        when(formListDownloader.downloadFormList(true)).thenReturn(formListResponse);

        formDownloader = mock(FormDownloader.class);

        synchronizer = new ServerFormListSynchronizer(formRepository, formListDownloader, formDownloader);
    }

    @Test
    public void whenNoFormsExist_downloadsAndSavesAllFormsInList() {
        synchronizer.synchronize();
        verify(formDownloader).downloadForms(eq(asList(formList.get(0))), any());
        verify(formDownloader).downloadForms(eq(asList(formList.get(1))), any());
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
        verify(formDownloader).downloadForms(eq(asList(formList.get(1))), any());
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
        verify(formDownloader, never()).downloadForms(eq(asList(formList.get(1))), any());
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