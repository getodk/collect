package org.odk.collect.android.instancemanagement;

import android.util.Pair;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.gdrive.InstanceGoogleSheetsUploader;
import org.odk.collect.android.instancemanagement.SubmitException.Type;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.upload.InstanceServerUploader;
import org.odk.collect.android.upload.InstanceUploader;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.shared.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;

public class InstanceSubmitter {

    private final Analytics analytics;
    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final GoogleAccountsManager googleAccountsManager;
    private final GoogleApiProvider googleApiProvider;
    private final PermissionsProvider permissionsProvider;
    private final Settings generalSettings;

    public InstanceSubmitter(Analytics analytics, FormsRepository formsRepository, InstancesRepository instancesRepository,
                             GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider, Settings generalSettings) {
        this.analytics = analytics;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.googleAccountsManager = googleAccountsManager;
        this.googleApiProvider = googleApiProvider;
        this.permissionsProvider = permissionsProvider;
        this.generalSettings = generalSettings;
    }

    public Pair<Boolean, String> submitInstances(List<Instance> toUpload) throws SubmitException {
        if (toUpload.isEmpty()) {
            throw new SubmitException(Type.NOTHING_TO_SUBMIT);
        }

        String protocol = generalSettings.getString(ProjectKeys.KEY_PROTOCOL);

        InstanceUploader uploader;
        Map<String, String> resultMessagesByInstanceId = new HashMap<>();
        String deviceId = null;
        boolean anyFailure = false;

        if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
            if (permissionsProvider.isGetAccountsPermissionGranted()) {
                String googleUsername = googleAccountsManager.getLastSelectedAccountIfValid();
                if (googleUsername.isEmpty()) {
                    throw new SubmitException(Type.GOOGLE_ACCOUNT_NOT_SET);
                }
                googleAccountsManager.selectAccount(googleUsername);
                uploader = new InstanceGoogleSheetsUploader(googleApiProvider.getDriveApi(googleUsername), googleApiProvider.getSheetsApi(googleUsername));
            } else {
                throw new SubmitException(Type.GOOGLE_ACCOUNT_NOT_PERMITTED);
            }
        } else {
            OpenRosaHttpInterface httpInterface = Collect.getInstance().getComponent().openRosaHttpInterface();
            uploader = new InstanceServerUploader(httpInterface, new WebCredentialsUtils(generalSettings), new HashMap<>(), generalSettings);
            deviceId = new PropertyManager().getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);
        }

        for (Instance instance : toUpload) {
            try {
                String destinationUrl;
                if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
                    destinationUrl = uploader.getUrlToSubmitTo(instance, null, null, generalSettings.getString(KEY_GOOGLE_SHEETS_URL));

                    if (!InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        anyFailure = true;
                        resultMessagesByInstanceId.put(instance.getDbId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                        continue;
                    }
                } else {
                    destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null, null);
                }

                String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                resultMessagesByInstanceId.put(instance.getDbId().toString(), customMessage != null ? customMessage : TranslationHandler.getString(Collect.getInstance(), R.string.success));

                // If the submission was successful, delete the instance if either the app-level
                // delete preference is set or the form definition requests auto-deletion.
                // TODO: this could take some time so might be better to do in a separate process,
                // perhaps another worker. It also feels like this could fail and if so should be
                // communicated to the user. Maybe successful delete should also be communicated?
                if (InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, instance.getFormId(), instance.getFormVersion(),
                        generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND))) {
                    new InstanceDeleter(new InstancesRepositoryProvider(Collect.getInstance()).get(), new FormsRepositoryProvider(Collect.getInstance()).get()).delete(instance.getDbId());
                }

                String action = protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS) ?
                        "HTTP-Sheets auto" : "HTTP auto";
                String label = Collect.getFormIdentifierHash(instance.getFormId(), instance.getFormVersion());
                analytics.logEvent(SUBMISSION, action, label);
            } catch (UploadException e) {
                Timber.d(e);
                anyFailure = true;
                resultMessagesByInstanceId.put(instance.getDbId().toString(),
                        e.getDisplayMessage());
            }
        }

        return new Pair<>(anyFailure, InstanceUploaderUtils.getUploadResultMessage(instancesRepository, Collect.getInstance(), resultMessagesByInstanceId));
    }
}
