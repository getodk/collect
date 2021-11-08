package org.odk.collect.android.formmanagement;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.external.FormsContract.getUri;

public class BlankFormsListViewModel extends ViewModel {

    private final Application application;
    private final Scheduler scheduler;
    private final SyncStatusAppState syncRepository;
    private final Settings generalSettings;
    private final Analytics analytics;
    private final FormsUpdater formsUpdater;
    private final CurrentProjectProvider currentProjectProvider;
    private final FormsRepositoryProvider formsRepositoryProvider;

    public BlankFormsListViewModel(Application application, Scheduler scheduler, SyncStatusAppState syncRepository, SettingsProvider settingsProvider, Analytics analytics, FormsUpdater formsUpdater, CurrentProjectProvider currentProjectProvider, FormsRepositoryProvider formsRepositoryProvider) {
        this.application = application;
        this.scheduler = scheduler;
        this.syncRepository = syncRepository;
        this.generalSettings = settingsProvider.getUnprotectedSettings();
        this.analytics = analytics;
        this.formsUpdater = formsUpdater;
        this.currentProjectProvider = currentProjectProvider;
        this.formsRepositoryProvider = formsRepositoryProvider;
    }

    public List<BlankForm> getForms() {
        return formsRepositoryProvider.get()
                .getAll()
                .stream()
                .map(form -> new BlankForm(form, currentProjectProvider))
                .collect(Collectors.toList());
    }

    public boolean isMatchExactlyEnabled() {
        return getFormUpdateMode(application, generalSettings) == FormUpdateMode.MATCH_EXACTLY;
    }

    public LiveData<Boolean> isSyncing() {
        return syncRepository.isSyncing(getProjectId());
    }

    public LiveData<Boolean> isOutOfSync() {
        return Transformations.map(syncRepository.getSyncError(getProjectId()), Objects::nonNull);
    }

    public LiveData<Boolean> isAuthenticationRequired() {
        return Transformations.map(syncRepository.getSyncError(getProjectId()), error -> {
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
        scheduler.immediate(() -> formsUpdater.matchFormsWithServer(getProjectId()), result::setValue);
        return result;
    }

    @NotNull
    private String getProjectId() {
        return currentProjectProvider.getCurrentProject().getUuid();
    }

    private void logManualSync() {
        Uri uri = Uri.parse(generalSettings.getString(ProjectKeys.KEY_SERVER_URL));
        String host = uri.getHost() != null ? uri.getHost() : "";
        String urlHash = Md5.getMd5Hash(new ByteArrayInputStream(host.getBytes()));
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC, "Manual", urlHash);
    }

    public static class BlankForm {

        private final String name;
        private final Uri contentUri;

        private BlankForm(Form form, CurrentProjectProvider currentProjectProvider) {
            this.name = form.getDisplayName();
            this.contentUri = getUri(currentProjectProvider.getCurrentProject().getUuid(), form.getDbId());
        }

        public String getName() {
            return name;
        }

        public Uri getContentUri() {
            return contentUri;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final Scheduler scheduler;
        private final SyncStatusAppState syncRepository;
        private final SettingsProvider settingsProvider;
        private final Analytics analytics;
        private final FormsUpdater formsUpdater;
        private final CurrentProjectProvider currentProjectProvider;
        private final FormsRepositoryProvider formsRepositoryProvider;

        @Inject
        public Factory(Application application, Scheduler scheduler, SyncStatusAppState syncRepository, SettingsProvider settingsProvider, Analytics analytics, FormsUpdater formsUpdater, CurrentProjectProvider currentProjectProvider, FormsRepositoryProvider formsRepositoryProvider) {
            this.application = application;
            this.scheduler = scheduler;
            this.syncRepository = syncRepository;
            this.settingsProvider = settingsProvider;
            this.analytics = analytics;
            this.formsUpdater = formsUpdater;
            this.currentProjectProvider = currentProjectProvider;
            this.formsRepositoryProvider = formsRepositoryProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BlankFormsListViewModel(application, scheduler, syncRepository, settingsProvider, analytics, formsUpdater, currentProjectProvider, formsRepositoryProvider);
        }
    }
}
