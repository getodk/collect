package org.odk.collect.android.formmanagement;

import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.InMemInstancesRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormsSynchronizerTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();
    private final InstancesRepository instancesRepository = new InMemInstancesRepository();
    private final RecordingFormDownloader formDownloader = new RecordingFormDownloader();
    private final ServerFormsDetailsFetcher serverFormDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
    private final ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(serverFormDetailsFetcher, formsRepository, instancesRepository, formDownloader);

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
        formsRepository.save(new Form.Builder()
                .id(3L)
                .jrFormId("form-3")
                .md5Hash("form-3-hash")
                .build());

        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, false, false)
        ));

        synchronizer.synchronize();
        assertThat(formsRepository.contains("form-3"), is(false));
    }

    @Test
    public void doesNotDownloadExistingForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, false, false)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), is(empty()));
    }

    @Test
    public void whenFetchingFormDetailsThrowsAnError_throwsError() throws Exception {
        FormApiException exception = new FormApiException(FormApiException.Type.AUTH_REQUIRED);
        when(serverFormDetailsFetcher.fetchFormDetails()).thenThrow(exception);

        try {
            synchronizer.synchronize();
        } catch (FormApiException e) {
            assertThat(e, is(exception));
        }
    }

    @Test
    public void whenDownloadingFormThrowsAnError_throwsErrorAndDownloadsOtherForms() throws Exception {
        List<ServerFormDetails> serverForms = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", null, true, false),
                new ServerFormDetails("form-2", "http://example.com/form-2", null, "form-2", "server", "md5:form-2-hash", null, true, false)
        );

        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(serverForms);

        FormDownloader formDownloader = mock(FormDownloader.class);
        doThrow(new FormDownloadException()).when(formDownloader).downloadForm(serverForms.get(0));

        ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(serverFormDetailsFetcher, formsRepository, instancesRepository, formDownloader);

        try {
            synchronizer.synchronize();
        } catch (FormApiException e) {
            assertThat(e.getType(), is(FormApiException.Type.FETCH_ERROR));
            verify(formDownloader).downloadForm(serverForms.get(1));
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