package org.odk.collect.android.instancemanagement;

import android.content.Context;
import android.util.Pair;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.formmanagement.InstancesAppState;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.ChangeLockProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.shared.Settings;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.utilities.InstanceUploaderUtils.shouldFormBeSent;

public class InstanceAutoSender {

    private final Context context;
    private final ChangeLockProvider changeLockProvider;
    private final Notifier notifier;
    private final Analytics analytics;
    private final FormsRepositoryProvider formsRepositoryProvider;
    private final InstancesRepositoryProvider instancesRepositoryProvider;
    private final GoogleAccountsManager googleAccountsManager;
    private final GoogleApiProvider googleApiProvider;
    private final PermissionsProvider permissionsProvider;
    private final SettingsProvider settingsProvider;
    private final InstancesAppState instancesAppState;

    public InstanceAutoSender(Context context, ChangeLockProvider changeLockProvider, Notifier notifier, Analytics analytics, FormsRepositoryProvider formsRepositoryProvider, InstancesRepositoryProvider instancesRepositoryProvider, GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider, SettingsProvider settingsProvider, InstancesAppState instancesAppState) {
        this.context = context;
        this.changeLockProvider = changeLockProvider;
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

    public boolean autoSendInstances(String projectId) {
        FormsRepository formsRepository = formsRepositoryProvider.get(projectId);
        InstancesRepository instancesRepository = instancesRepositoryProvider.get(projectId);
        Settings generalSettings = settingsProvider.getUnprotectedSettings(projectId);
        InstanceSubmitter instanceSubmitter = new InstanceSubmitter(analytics, formsRepository, instancesRepository, googleAccountsManager, googleApiProvider, permissionsProvider, generalSettings);

        return changeLockProvider.getInstanceLock(projectId).withLock(acquiredLock -> {
            if (acquiredLock) {
                try {
                    List<Instance> toUpload = getInstancesToAutoSend(formsRepository, instancesRepository, generalSettings);
                    Pair<Boolean, String> results = instanceSubmitter.submitInstances(toUpload);
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

    @NotNull
    private List<Instance> getInstancesToAutoSend(FormsRepository formsRepository, InstancesRepository instancesRepository, Settings generalSettings) {
        boolean isAutoSendAppSettingEnabled = !generalSettings.getString(ProjectKeys.KEY_AUTOSEND).equals("off");
        List<Instance> toUpload = new ArrayList<>();
        for (Instance instance : instancesRepository.getAllByStatus(Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED)) {
            if (shouldFormBeSent(formsRepository, instance.getFormId(), instance.getFormVersion(), isAutoSendAppSettingEnabled)) {
                toUpload.add(instance);
            }
        }

        return toUpload;
    }
}
