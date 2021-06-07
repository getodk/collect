package org.odk.collect.android.instancemanagement;

import android.content.Context;
import android.util.Pair;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.formmanagement.InstancesAppState;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.locks.ChangeLock;

public class InstanceAutoSender {

    private final Context context;
    private final ChangeLock changeLock;
    private final Notifier notifier;
    private final Analytics analytics;
    private final FormsRepositoryProvider formsRepositoryProvider;
    private final InstancesRepositoryProvider instancesRepositoryProvider;
    private final GoogleAccountsManager googleAccountsManager;
    private final GoogleApiProvider googleApiProvider;
    private final PermissionsProvider permissionsProvider;
    private final SettingsProvider settingsProvider;
    private final InstancesAppState instancesAppState;

    public InstanceAutoSender(Context context, ChangeLock changeLock, Notifier notifier, Analytics analytics, FormsRepositoryProvider formsRepositoryProvider, InstancesRepositoryProvider instancesRepositoryProvider, GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider, SettingsProvider settingsProvider, InstancesAppState instancesAppState) {
        this.context = context;
        this.changeLock = changeLock;
        this.notifier = notifier;
        this.analytics = analytics;
        this.formsRepositoryProvider = formsRepositoryProvider;
        this.instancesRepositoryProvider = instancesRepositoryProvider;
        this.googleAccountsManager = googleAccountsManager;
        this.googleApiProvider = googleApiProvider;
        this.permissionsProvider = permissionsProvider;
        this.settingsProvider = settingsProvider;
        this.instancesAppState = instancesAppState;
    }

    public boolean autoSendInstances() {
        FormsRepository formsRepository = formsRepositoryProvider.get();
        InstancesRepository instancesRepository = instancesRepositoryProvider.get();
        Settings generalSettings = settingsProvider.getGeneralSettings();
        InstanceSubmitter instanceSubmitter = new InstanceSubmitter(analytics, formsRepository, instancesRepository, googleAccountsManager, googleApiProvider, permissionsProvider, generalSettings);

        return changeLock.withLock(acquiredLock -> {
            if (acquiredLock) {
                try {
                    Pair<Boolean, String> results = instanceSubmitter.submitUnsubmittedInstances();
                    notifier.onSubmission(results.first, results.second);
                } catch (SubmitException e) {
                    switch (e.getType()) {
                        case GOOGLE_ACCOUNT_NOT_SET:
                            notifier.onSubmission(true, context.getString(R.string.google_set_account));
                            break;
                        case GOOGLE_ACCOUNT_NOT_PERMITTED:
                            notifier.onSubmission(true, context.getString(R.string.odk_permissions_fail));
                            break;
                        case NOTHING_TO_SUBMIT:
                            break;
                    }
                }

                instancesAppState.update();
                return true;
            } else {
                return false;
            }
        });
    }
}
