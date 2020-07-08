package org.odk.collect.android.formmanagement;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.async.Scheduler;

import javax.inject.Inject;

public class BlankFormsListViewModel extends ViewModel {

    private final Scheduler scheduler;
    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormListApi formListAPI;
    private final FormDownloader multiFormDownloader;
    private final DiskFormsSynchronizer diskFormsSynchronizer;

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);

    public BlankFormsListViewModel(Scheduler scheduler, FormRepository formRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, FormDownloader formDownloader, DiskFormsSynchronizer diskFormsSynchronizer) {
        this.scheduler = scheduler;
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formListAPI = formListAPI;
        this.multiFormDownloader = formDownloader;
        this.diskFormsSynchronizer = diskFormsSynchronizer;
    }

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public void syncWithServer() {
        syncing.setValue(true);

        scheduler.runInBackground(() -> {
            try {
                ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(formRepository, mediaFileRepository, formListAPI, multiFormDownloader, diskFormsSynchronizer);
                synchronizer.synchronize();
            } catch (FormApiException ignored) {
                // Ignored
            }

            return null;
        }, ignored -> syncing.setValue(false));
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Scheduler scheduler;
        private final FormRepository formRepository;
        private final MediaFileRepository mediaFileRepository;
        private final FormListApi formListAPI;
        private final FormDownloader formDownloader;
        private final DiskFormsSynchronizer diskFormsSynchronizer;

        @Inject
        public Factory(Scheduler scheduler, FormRepository formRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, FormDownloader formDownloader, DiskFormsSynchronizer diskFormsSynchronizer) {
            this.scheduler = scheduler;
            this.formRepository = formRepository;
            this.mediaFileRepository = mediaFileRepository;
            this.formListAPI = formListAPI;
            this.formDownloader = formDownloader;
            this.diskFormsSynchronizer = diskFormsSynchronizer;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BlankFormsListViewModel(scheduler, formRepository, mediaFileRepository, formListAPI, formDownloader, diskFormsSynchronizer);
        }
    }
}
