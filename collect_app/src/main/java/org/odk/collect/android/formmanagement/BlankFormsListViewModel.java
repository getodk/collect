package org.odk.collect.android.formmanagement;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.async.Scheduler;

import javax.inject.Inject;

public class BlankFormsListViewModel extends ViewModel {

    private final Scheduler scheduler;
    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormListApi formListAPI;
    private final FormDownloader multiFormDownloader;
    private final DiskFormsSynchronizer diskFormsSynchronizer;
    private final ServerFormsSyncRepository syncRepository;
    private final PreferencesProvider preferencesProvider;

    public BlankFormsListViewModel(Scheduler scheduler, FormRepository formRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, FormDownloader formDownloader, DiskFormsSynchronizer diskFormsSynchronizer, ServerFormsSyncRepository syncRepository, PreferencesProvider preferencesProvider) {
        this.scheduler = scheduler;
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formListAPI = formListAPI;
        this.multiFormDownloader = formDownloader;
        this.diskFormsSynchronizer = diskFormsSynchronizer;
        this.syncRepository = syncRepository;
        this.preferencesProvider = preferencesProvider;
    }

    public boolean isSyncingAvailable() {
        SharedPreferences generalSharedPreferences = preferencesProvider.getGeneralSharedPreferences();
        return generalSharedPreferences.getBoolean(GeneralKeys.KEY_MATCH_EXACTLY, false);
    }

    public LiveData<Boolean> isSyncing() {
        return syncRepository.isSyncing();
    }

    public void syncWithServer() {
        syncRepository.startSync();

        scheduler.runInBackground(() -> {
            try {
                ServerFormsSynchronizer synchronizer = new ServerFormsSynchronizer(formRepository, mediaFileRepository, formListAPI, multiFormDownloader, diskFormsSynchronizer);
                synchronizer.synchronize();
            } catch (FormApiException ignored) {
                // Ignored
            }

            return null;
        }, ignored -> syncRepository.finishSync());
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Scheduler scheduler;
        private final FormRepository formRepository;
        private final MediaFileRepository mediaFileRepository;
        private final FormListApi formListAPI;
        private final FormDownloader formDownloader;
        private final DiskFormsSynchronizer diskFormsSynchronizer;
        private final ServerFormsSyncRepository syncRepository;
        private final PreferencesProvider preferencesProvider;

        @Inject
        public Factory(Scheduler scheduler,
                       FormRepository formRepository,
                       MediaFileRepository mediaFileRepository,
                       FormListApi formListAPI, FormDownloader formDownloader,
                       DiskFormsSynchronizer diskFormsSynchronizer,
                       ServerFormsSyncRepository syncRepository,
                       PreferencesProvider preferencesProvider
        ) {
            this.scheduler = scheduler;
            this.formRepository = formRepository;
            this.mediaFileRepository = mediaFileRepository;
            this.formListAPI = formListAPI;
            this.formDownloader = formDownloader;
            this.diskFormsSynchronizer = diskFormsSynchronizer;
            this.syncRepository = syncRepository;
            this.preferencesProvider = preferencesProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BlankFormsListViewModel(scheduler, formRepository, mediaFileRepository, formListAPI, formDownloader, diskFormsSynchronizer, syncRepository, preferencesProvider);
        }
    }
}
