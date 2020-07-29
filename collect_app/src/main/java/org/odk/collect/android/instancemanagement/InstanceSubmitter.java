package org.odk.collect.android.instancemanagement;

import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.instancemanagement.SubmitException.Type;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.upload.InstanceGoogleSheetsUploader;
import org.odk.collect.android.upload.InstanceServerUploader;
import org.odk.collect.android.upload.InstanceUploader;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;

public class InstanceSubmitter {

    private final Analytics analytics;

    public InstanceSubmitter(Analytics analytics) {
        this.analytics = analytics;
    }

    public Pair<Boolean, String> submitUnsubmittedInstances() throws SubmitException {
        List<Instance> toUpload = getInstancesToAutoSend(GeneralSharedPreferences.isAutoSendEnabled());

        if (toUpload.isEmpty()) {
            throw new SubmitException(Type.NOTHING_TO_SUBMIT);
        }

        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
        String protocol = (String) settings.get(GeneralKeys.KEY_PROTOCOL);

        InstanceUploader uploader;
        Map<String, String> resultMessagesByInstanceId = new HashMap<>();
        String deviceId = null;
        boolean anyFailure = false;

        if (protocol.equals(Collect.getInstance().getString(R.string.protocol_google_sheets))) {
            if (PermissionUtils.isGetAccountsPermissionGranted(Collect.getInstance())) {
                GoogleAccountsManager accountsManager = new GoogleAccountsManager(Collect.getInstance());
                String googleUsername = accountsManager.getLastSelectedAccountIfValid();
                if (googleUsername.isEmpty()) {
                    throw new SubmitException(Type.GOOGLE_ACCOUNT_NOT_SET);
                }
                accountsManager.selectAccount(googleUsername);
                uploader = new InstanceGoogleSheetsUploader(accountsManager);
            } else {
                throw new SubmitException(Type.GOOGLE_ACCOUNT_NOT_PERMITTED);
            }
        } else {
            OpenRosaHttpInterface httpInterface = Collect.getInstance().getComponent().openRosaHttpInterface();
            uploader = new InstanceServerUploader(httpInterface,
                    new WebCredentialsUtils(), new HashMap<>());
            deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));
        }

        for (Instance instance : toUpload) {
            try {
                String destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null);
                if (protocol.equals(Collect.getInstance().getString(R.string.protocol_google_sheets))
                        && !InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                    anyFailure = true;
                    resultMessagesByInstanceId.put(instance.getId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                    continue;
                }
                String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                resultMessagesByInstanceId.put(instance.getId().toString(), customMessage != null ? customMessage : Collect.getInstance().getString(R.string.success));

                // If the submission was successful, delete the instance if either the app-level
                // delete preference is set or the form definition requests auto-deletion.
                // TODO: this could take some time so might be better to do in a separate process,
                // perhaps another worker. It also feels like this could fail and if so should be
                // communicated to the user. Maybe successful delete should also be communicated?
                if (InstanceUploader.formShouldBeAutoDeleted(instance.getJrFormId(),
                        (boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_DELETE_AFTER_SEND))) {
                    Uri deleteForm = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instance.getId().toString());
                    Collect.getInstance().getContentResolver().delete(deleteForm, null, null);
                }

                String action = protocol.equals(Collect.getInstance().getString(R.string.protocol_google_sheets)) ?
                        "HTTP-Sheets auto" : "HTTP auto";
                String label = Collect.getFormIdentifierHash(instance.getJrFormId(), instance.getJrVersion());
                analytics.logEvent(SUBMISSION, action, label);
            } catch (UploadException e) {
                Timber.d(e);
                anyFailure = true;
                resultMessagesByInstanceId.put(instance.getId().toString(),
                        e.getDisplayMessage());
            }
        }

        return new Pair<>(anyFailure, InstanceUploaderUtils.getUploadResultMessage(Collect.getInstance(), resultMessagesByInstanceId));
    }

    /**
     * Returns instances that need to be auto-sent.
     */
    @NonNull
    private List<Instance> getInstancesToAutoSend(boolean isAutoSendAppSettingEnabled) {
        InstancesDao dao = new InstancesDao();
        Cursor c = dao.getFinalizedInstancesCursor();
        List<Instance> allFinalized = dao.getInstancesFromCursor(c);

        List<Instance> toUpload = new ArrayList<>();
        for (Instance instance : allFinalized) {
            if (formShouldBeAutoSent(instance.getJrFormId(), isAutoSendAppSettingEnabled)) {
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
     */
    public static boolean formShouldBeAutoSent(String jrFormId, boolean isAutoSendAppSettingEnabled) {
        Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId);
        String formLevelAutoSend = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
                formLevelAutoSend = cursor.getString(autoSendColumnIndex);
            } finally {
                cursor.close();
            }
        }

        return formLevelAutoSend == null ? isAutoSendAppSettingEnabled
                : Boolean.valueOf(formLevelAutoSend);
    }
}
