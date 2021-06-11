package org.odk.collect.android.formmanagement;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.shared.Settings;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import javax.inject.Inject;

import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;

public class BlankFormsListViewModel extends ViewModel {

    private final Application application;
    private final Scheduler scheduler;
    private final SyncStatusAppState syncRepository;
    private final Settings generalSettings;
    private final Analytics analytics;
    private final FormsUpdater formsUpdater;
    private final CurrentProjectProvider currentProjectProvider;

    public BlankFormsListViewModel(Application application, Scheduler scheduler, SyncStatusAppState syncRepository, SettingsProvider settingsProvider, Analytics analytics, FormsUpdater formsUpdater, CurrentProjectProvider currentProjectProvider) {
        this.application = application;
        this.scheduler = scheduler;
        this.syncRepository = syncRepository;
        this.generalSettings = settingsProvider.getGeneralSettings();
        this.analytics = analytics;
        this.formsUpdater = formsUpdater;
        this.currentProjectProvider = currentProjectProvider;
    }

    public boolean isMatchExactlyEnabled() {
        return getFormUpdateMode(application, generalSettings) == FormUpdateMode.MATCH_EXACTLY;
    }

    public LiveData<Boolean> isSyncing() {
        return syncRepository.isSyncing();
    }

    public LiveData<Boolean> isOutOfSync() {
        return Transformations.map(syncRepository.getSyncError(), Objects::nonNull);
    }

    public LiveData<Boolean> isAuthenticationRequired() {
        return Transformations.map(syncRepository.getSyncError(), error -> {
            if (error != null) {
                return error instanceof FormSourceException.AuthRequired;
            } else {
                return false;
            }
        });
    }

    public LiveData<Boolean> syncWithServer() {
        logManualSync();

        MutableLiveData<Boolean> result = new MutableLiveData<>();
        scheduler.immediate(() -> formsUpdater.matchFormsWithServer(currentProjectProvider.getCurrentProject().getUuid()), result::setValue);
        return result;
    }

    private void logManualSync() {
        Uri uri = Uri.parse(generalSettings.getString(GeneralKeys.KEY_SERVER_URL));
        String host = uri.getHost() != null ? uri.getHost() : "";
        String urlHash = Md5.getMd5Hash(new ByteArrayInputStream(host.getBytes()));
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC, "Manual", urlHash);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final Scheduler scheduler;
        private final SyncStatusAppState syncRepository;
        private final SettingsProvider settingsProvider;
        private final Analytics analytics;
        private final FormsUpdater formsUpdater;
        private final CurrentProjectProvider currentProjectProvider;

        @Inject
        public Factory(Application application, Scheduler scheduler, SyncStatusAppState syncRepository, SettingsProvider settingsProvider, Analytics analytics, FormsUpdater formsUpdater, CurrentProjectProvider currentProjectProvider) {
            this.application = application;
            this.scheduler = scheduler;
            this.syncRepository = syncRepository;
            this.settingsProvider = settingsProvider;
            this.analytics = analytics;
            this.formsUpdater = formsUpdater;
            this.currentProjectProvider = currentProjectProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BlankFormsListViewModel(application, scheduler, syncRepository, settingsProvider, analytics, formsUpdater, currentProjectProvider);
        }
    }
}
