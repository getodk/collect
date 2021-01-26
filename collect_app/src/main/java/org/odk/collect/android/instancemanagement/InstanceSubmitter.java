package org.odk.collect.android.instancemanagement;

import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.gdrive.InstanceGoogleSheetsUploader;
import org.odk.collect.android.instancemanagement.SubmitException.Type;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.upload.InstanceServerUploader;
import org.odk.collect.android.upload.InstanceUploader;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.CUSTOM_ENDPOINT_SUB;
import static org.odk.collect.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;

public class InstanceSubmitter {

    private final Analytics analytics;
    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final GoogleAccountsManager googleAccountsManager;
    private final GoogleApiProvider googleApiProvider;
    private final PermissionsProvider permissionsProvider;

    public InstanceSubmitter(Analytics analytics, FormsRepository formsRepository, InstancesRepository instancesRepository,
                             GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider) {
        this.analytics = analytics;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.googleAccountsManager = googleAccountsManager;
        this.googleApiProvider = googleApiProvider;
        this.permissionsProvider = permissionsProvider;
    }

    public Pair<Boolean, String> submitUnsubmittedInstances() throws SubmitException {
        List<Instance> toUpload = getInstancesToAutoSend(GeneralSharedPreferences.isAutoSendEnabled());
        return submitSelectedInstances(toUpload);
    }

    public Pair<Boolean, String> submitSelectedInstances(List<Instance> toUpload) throws SubmitException {
        if (toUpload.isEmpty()) {
            throw new SubmitException(Type.NOTHING_TO_SUBMIT);
        }

        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
        String protocol = (String) settings.get(GeneralKeys.KEY_PROTOCOL);

        InstanceUploader uploader;
        Map<String, String> resultMessagesByInstanceId = new HashMap<>();
        String deviceId = null;
        boolean anyFailure = false;

        if (protocol.equals(TranslationHandler.getString(Collect.getInstance(), R.string.protocol_google_sheets))) {
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
            uploader = new InstanceServerUploader(httpInterface,
                    new WebCredentialsUtils(), new HashMap<>());
            deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);
        }

        for (Instance instance : toUpload) {
            try {
                String destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null);
                if (protocol.equals(TranslationHandler.getString(Collect.getInstance(), R.string.protocol_google_sheets))
                        && !InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                    anyFailure = true;
                    resultMessagesByInstanceId.put(instance.getId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                    continue;
                }
                String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                resultMessagesByInstanceId.put(instance.getId().toString(), customMessage != null ? customMessage : TranslationHandler.getString(Collect.getInstance(), R.string.success));

                // If the submission was successful, delete the instance if either the app-level
                // delete preference is set or the form definition requests auto-deletion.
                // TODO: this could take some time so might be better to do in a separate process,
                // perhaps another worker. It also feels like this could fail and if so should be
                // communicated to the user. Maybe successful delete should also be communicated?
                if (InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, instance.getJrFormId(), instance.getJrVersion(),
                        (boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_DELETE_AFTER_SEND))) {
                    Uri deleteForm = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instance.getId().toString());
                    Collect.getInstance().getContentResolver().delete(deleteForm, null, null);
                }

                String action = protocol.equals(TranslationHandler.getString(Collect.getInstance(), R.string.protocol_google_sheets)) ?
                        "HTTP-Sheets auto" : "HTTP auto";
                String label = Collect.getFormIdentifierHash(instance.getJrFormId(), instance.getJrVersion());
                analytics.logEvent(SUBMISSION, action, label);

                String submissionEndpoint = (String) settings.get(GeneralKeys.KEY_SUBMISSION_URL);
                if (!submissionEndpoint.equals(TranslationHandler.getString(Collect.getInstance(), R.string.default_odk_submission))) {
                    String submissionEndpointHash = FileUtils.getMd5Hash(new ByteArrayInputStream(submissionEndpoint.getBytes()));
                    analytics.logEvent(CUSTOM_ENDPOINT_SUB, submissionEndpointHash);
                }
            } catch (UploadException e) {
                Timber.d(e);
                anyFailure = true;
                resultMessagesByInstanceId.put(instance.getId().toString(),
                        e.getDisplayMessage());
            }
        }

        return new Pair<>(anyFailure, InstanceUploaderUtils.getUploadResultMessage(instancesRepository, Collect.getInstance(), resultMessagesByInstanceId));
    }

    /**
     * Returns instances that need to be auto-sent.
     */
    @NonNull
    private List<Instance> getInstancesToAutoSend(boolean isAutoSendAppSettingEnabled) {
        List<Instance> toUpload = new ArrayList<>();
        for (Instance instance : instancesRepository.getAllFinalized()) {
            if (shouldFormBeSent(formsRepository, instance.getJrFormId(), instance.getJrVersion(), isAutoSendAppSettingEnabled)) {
                toUpload.add(instance);
            }
        }

        return toUpload;
    }

    /**
     * Returns whether a form with the specified form_id should be auto-sent given the current
     * app-level auto-send settings. Returns false if there is no form with the specified form_id.
     *
     * A form should be auto-sent if auto-send is on at the app level AND this form doesn't override
     * auto-send settings OR if auto-send is on at the form-level.
     *
     * @param isAutoSendAppSettingEnabled whether the auto-send option is enabled at the app level
     *
     * @deprecated should be private what requires refactoring the whole class to make it testable
     */
    @Deprecated
    public static boolean shouldFormBeSent(FormsRepository formsRepository, String jrFormId, String jrFormVersion, boolean isAutoSendAppSettingEnabled) {
        Form form = formsRepository.getOneByFormIdAndVersion(jrFormId, jrFormVersion);
        if (form == null) {
            return false;
        }
        return form.getAutoSend() == null ? isAutoSendAppSettingEnabled : Boolean.valueOf(form.getAutoSend());
    }
}
