package org.odk.collect.android.formmanagement;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.async.Scheduler;

import javax.inject.Inject;

public class BlankFormsListViewModel extends ViewModel {

    private final Scheduler scheduler;
    private final SyncStatusRepository syncRepository;
    private final ServerFormsSynchronizer serverFormsSynchronizer;
    private final PreferencesProvider preferencesProvider;

    public BlankFormsListViewModel(Scheduler scheduler, SyncStatusRepository syncRepository, ServerFormsSynchronizer serverFormsSynchronizer, PreferencesProvider preferencesProvider) {
        this.scheduler = scheduler;
        this.syncRepository = syncRepository;
        this.serverFormsSynchronizer = serverFormsSynchronizer;
        this.preferencesProvider = preferencesProvider;
    }

    public boolean isSyncingAvailable() {
        return isMatchExactlyEnabled();
    }

    public LiveData<Boolean> isSyncing() {
        return syncRepository.isSyncing();
    }

    public void syncWithServer() {
        if (!syncRepository.startSync()) {
            return;
        }

        scheduler.immediate(() -> {
            try {
                serverFormsSynchronizer.synchronize();
            } catch (FormApiException ignored) {
                // Ignored
            }

            return null;
        }, ignored -> syncRepository.finishSync());
    }

    private boolean isMatchExactlyEnabled() {
        SharedPreferences generalSharedPreferences = preferencesProvider.getGeneralSharedPreferences();
        return "match_exactly".equals(generalSharedPreferences.getString(GeneralKeys.KEY_FORM_UPDATE_MODE, null));
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Scheduler scheduler;
        private final SyncStatusRepository syncRepository;
        private final ServerFormsSynchronizer serverFormsSynchronizer;
        private final PreferencesProvider preferencesProvider;

        @Inject
        public Factory(Scheduler scheduler, SyncStatusRepository syncRepository, ServerFormsSynchronizer serverFormsSynchronizer, PreferencesProvider preferencesProvider) {
            this.scheduler = scheduler;
            this.syncRepository = syncRepository;
            this.serverFormsSynchronizer = serverFormsSynchronizer;
            this.preferencesProvider = preferencesProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BlankFormsListViewModel(scheduler, syncRepository, serverFormsSynchronizer, preferencesProvider);
        }
    }
}
