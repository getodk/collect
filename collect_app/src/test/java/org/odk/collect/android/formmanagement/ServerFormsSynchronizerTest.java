package org.odk.collect.android.formmanagement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.odk.collect.android.formmanagement.download.FormDownloadException;
import org.odk.collect.android.formmanagement.download.FormDownloader;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.FormUtils;
import org.odk.collect.formstest.InMemFormsRepository;
import org.odk.collect.formstest.InMemInstancesRepository;
import org.odk.collect.shared.TempFiles;
import org.odk.collect.shared.result.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import kotlin.Pair;
import kotlin.Unit;

public class ServerFormsSynchronizerTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();
    private final InstancesRepository instancesRepository = new InMemInstancesRepository();
    private final RecordingFormDownloader formDownloader = new RecordingFormDownloader();
    private final ServerFormsSynchronizer.ServerFormsDetailsFetcher serverFormDetailsFetcher = mock(ServerFormsSynchronizer.ServerFormsDetailsFetcher.class);
    private final FormSource formSource = mock(FormSource.class);
    private final ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(serverFormDetailsFetcher, formsRepository, instancesRepository, formDownloader, formSource);

    @Test
    public void downloadsNewForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "form-1-hash", true, false, null)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1"));
    }

    @Test
    public void downloadsUpdatedForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "form-1-hash", false, true, null)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), containsInAnyOrder("form-1"));
    }

    @Test
    public void deletesFormsNotInList() throws Exception {
        formsRepository.save(new Form.Builder()
                .dbId(3L)
                .formId("form-3")
                .md5Hash("form-3-hash")
                .formFilePath(FormUtils.createXFormFile("1", "1").getAbsolutePath())
                .build());

        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "form-1-hash", false, false, null)
        ));

        synchronizer.synchronize();
        assertThat(formsRepository.getAll().isEmpty(), is(true));
    }

    @Test
    public void doesNotDownloadExistingForms() throws Exception {
        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenReturn(asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "form-1-hash", false, false, null)
        ));

        synchronizer.synchronize();
        assertThat(formDownloader.getDownloadedForms(), is(empty()));
    }

    @Test
    public void whenFetchingFormDetailsThrowsAnError_throwsError() throws Exception {
        FormSourceException exception = new FormSourceException.AuthRequired();
        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenThrow(exception);

        try {
            synchronizer.synchronize();
        } catch (FormSourceException e) {
            assertThat(e, is(exception));
        }
    }

    @Test
    public void whenDownloadingFormThrowsAnError_throwsErrorAndDownloadsOtherForms() throws Exception {
        List<ServerFormDetails> serverForms = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "form-1-hash", true, false, null),
                new ServerFormDetails("form-2", "http://example.com/form-2", "form-2", "server", "form-2-hash", true, false, null)
        );

        when(serverFormDetailsFetcher.fetchFormDetails(formsRepository, formSource)).thenReturn(serverForms);

        FormDownloader formDownloader = mock(FormDownloader.class);
        doThrow(new FormDownloadException.FormSourceError(new FormSourceException.FetchError())).when(formDownloader).downloadAndProcessForm(serverForms.get(0), null, null);

        ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(serverFormDetailsFetcher, formsRepository, instancesRepository, formDownloader, formSource);

        try {
            synchronizer.synchronize();
        } catch (FormSourceException.FetchError e) {
            verify(formDownloader).downloadAndProcessForm(serverForms.get(1), null, null);
        }
    }

    private static class RecordingFormDownloader implements FormDownloader {

        private final List<String> formsProcessed = new ArrayList<>();

        public List<String> getDownloadedForms() {
            return formsProcessed;
        }

        @Override
        public @NotNull Result<@NotNull Pair<@NotNull FormFileDownload, @NotNull MediaFilesDownload>, @NotNull FormDownloadException> downloadForm(@NotNull ServerFormDetails form, @Nullable ProgressReporter progressReporter, @Nullable Supplier<@NotNull Boolean> isCancelled) {
            File tempDir = TempFiles.createTempDir();
            File formFile = TempFiles.createTempFile(tempDir, form.getFormId());
            File mediaDir = TempFiles.createTempDir(tempDir);
            return new Result.Success<>(
                    new Pair<>(new FormFileDownload.New(formFile, "blah", tempDir), new MediaFilesDownload(mediaDir.getAbsolutePath(), false, Collections.emptyList()))
            );
        }

        @Override
        public @NotNull Result<@NotNull Object, @NotNull FormDownloadException> processForm(@NotNull FormFileDownload formFileDownload, @NotNull MediaFilesDownload mediaFilesDownload) {
            FormFileDownload.New newDownload = (FormFileDownload.New) formFileDownload;
            formsProcessed.add(newDownload.getFile().getName());
            return new Result.Success<>(Unit.INSTANCE);
        }
    }
}
