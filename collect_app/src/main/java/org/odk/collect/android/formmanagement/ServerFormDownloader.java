package org.odk.collect.android.formmanagement;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Provides a sarcophagus for {@link org.odk.collect.android.utilities.MultiFormDownloader} so it
 * can eventually be disposed of.
 */
public class ServerFormDownloader implements FormDownloader {

    private final MultiFormDownloader multiFormDownloader;
    private final FormsRepository formsRepository;

    public ServerFormDownloader(FormListApi formListApi, FormsRepository formsRepository) {
        this.multiFormDownloader = new MultiFormDownloader(formsRepository, formListApi);
        this.formsRepository = formsRepository;
    }

    @Override
    public void downloadForm(ServerFormDetails form, @Nullable ProgressReporter progressReporter, @Nullable Supplier<Boolean> isCancelled) throws FormDownloadException {
        Form formOnDevice = formsRepository.get(form.getFormId(), form.getFormVersion());
        if (formOnDevice != null && formOnDevice.isDeleted()) {
            formsRepository.restore(formOnDevice.getId());
        }

        FormDownloaderListener stateListener = new ProgressReporterAndSupplierStateListener(progressReporter, isCancelled);

        try {
            HashMap<ServerFormDetails, String> results = multiFormDownloader.downloadForms(Collections.singletonList(form), stateListener);
            String result = new ArrayList<>(results.values()).get(0);

            if (!result.equals(Collect.getInstance().getString(R.string.success))) {
                throw new FormDownloadException(result);
            }
        } catch (MultiFormDownloader.TaskCancelledException e) {
            throw new CancellationException();
        }
    }

    private static class ProgressReporterAndSupplierStateListener implements FormDownloaderListener {
        private final ProgressReporter progressReporter;
        private final Supplier<Boolean> isCancelled;

        ProgressReporterAndSupplierStateListener(ProgressReporter progressReporter, Supplier<Boolean> isCancelled) {
            this.progressReporter = progressReporter;
            this.isCancelled = isCancelled;
        }

        @Override
        public void progressUpdate(String currentFile, String progress, String total) {
            if (progressReporter != null) {
                progressReporter.onDownloadingMediaFile(Integer.parseInt(progress));
            }
        }

        @Override
        public boolean isTaskCanceled() {
            if (isCancelled != null) {
                return isCancelled.get();
            } else {
                return false;
            }
        }
    }
}
