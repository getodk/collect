/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.instancemanagement.send;

import static java.util.Arrays.stream;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.activities.FormFillingActivity;
import org.odk.collect.android.fragments.dialogs.SimpleDialog;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.android.instancemanagement.InstancesDataService;
import org.odk.collect.android.projects.ProjectsDataService;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ArrayUtils;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.views.DayNightProgressDialog;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.metadata.PropertyManager;
import org.odk.collect.openrosa.http.OpenRosaConstants;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.strings.localization.LocalizedActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import kotlinx.coroutines.Dispatchers;
import timber.log.Timber;

/**
 * Activity to upload completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderActivity extends LocalizedActivity implements AuthDialogUtility.AuthDialogUtilityResultListener {
    private static final int PROGRESS_DIALOG = 1;
    private static final int AUTH_DIALOG = 2;

    private static final String AUTH_URI = "auth";
    private static final String TO_SEND = "tosend";

    private ProgressDialog progressDialog;

    // maintain a list of what we've yet to send, in case we're interrupted by auth requests
    private Long[] instancesToSend;

    // URL specified when authentication is requested or specified from intent extra as override
    private String url;

    private Boolean deleteInstanceAfterUpload;

    private boolean isInstanceStateSaved;

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Inject
    PropertyManager propertyManager;

    @Inject
    InstancesDataService instancesDataService;

    @Inject
    ProjectsDataService projectsDataService;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;
    private InstancesRepository instancesRepository;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;
    private FormsRepository formsRepository;

    @Inject
    SettingsProvider settingsProvider;

    private InstanceUploadViewModel instanceUploaderViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        instancesRepository = instancesRepositoryProvider.create();
        formsRepository = formsRepositoryProvider.create();

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        setTitle(getString(org.odk.collect.strings.R.string.send_data));

        // Get simple saved state
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(AUTH_URI);
        }

        Bundle dataBundle;

        // If we are resuming, use the TO_SEND list of not-yet-sent submissions
        // Otherwise, construct the list from the incoming intent value
        long[] selectedInstanceIDs;
        if (savedInstanceState != null && savedInstanceState.containsKey(TO_SEND)) {
            selectedInstanceIDs = savedInstanceState.getLongArray(TO_SEND);
            dataBundle = savedInstanceState;
        } else {
            selectedInstanceIDs = getIntent().getLongArrayExtra(FormFillingActivity.KEY_INSTANCES);
            dataBundle = getIntent().getExtras();

            boolean missingInstances = stream(selectedInstanceIDs).anyMatch(id -> instancesRepository.get(id) == null);
            if (missingInstances) {
                selectedInstanceIDs = new long[]{};
            }
        }

        // An external application can temporarily override destination URL, username, password
        // and whether instances should be deleted after submission by specifying intent extras.
        String externalUsername = null;
        String externalPassword = null;

        if (dataBundle != null && dataBundle.containsKey(ApplicationConstants.BundleKeys.URL)) {
            // TODO: I think this means redirection from a URL set through an extra is not supported
            url = dataBundle.getString(ApplicationConstants.BundleKeys.URL);

            // Remove trailing slashes (only necessary for the intent case but doesn't hurt on resume)
            while (url != null && url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (dataBundle.containsKey(ApplicationConstants.BundleKeys.USERNAME)
                    && dataBundle.containsKey(ApplicationConstants.BundleKeys.PASSWORD)) {
                externalUsername = dataBundle.getString(ApplicationConstants.BundleKeys.USERNAME);
                externalPassword = dataBundle.getString(ApplicationConstants.BundleKeys.PASSWORD);
            }

            if (dataBundle.containsKey(ApplicationConstants.BundleKeys.DELETE_INSTANCE_AFTER_SUBMISSION)) {
                deleteInstanceAfterUpload = dataBundle.getBoolean(ApplicationConstants.BundleKeys.DELETE_INSTANCE_AFTER_SUBMISSION);
            }
        }

        instancesToSend = ArrayUtils.toObject(selectedInstanceIDs);

        if (instancesToSend.length == 0) {
            Timber.e(new Error("onCreate: No instances to upload!"));
            // drop through -- everything will process through OK
        }

        showDialog(PROGRESS_DIALOG);

        if (url != null) {
            instanceUploaderViewModel.setCompleteDestinationUrl(url + OpenRosaConstants.SUBMISSION, getReferrerUri(), true);

            if (deleteInstanceAfterUpload != null) {
                instanceUploaderViewModel.setDeleteInstanceAfterSubmission(deleteInstanceAfterUpload);
            }
        }

        String finalExternalUsername = externalUsername;
        String finalExternalPassword = externalPassword;
        instanceUploaderViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        if (modelClass.isAssignableFrom(InstanceUploadViewModel.class)) {
                            return (T) new InstanceUploadViewModel(
                                    Dispatchers.getIO(),
                                    new ServerInstanceUploader(httpInterface, webCredentialsUtils, settingsProvider.getUnprotectedSettings(), instancesRepository),
                                    new InstanceDeleter(instancesRepository, formsRepository),
                                    webCredentialsUtils,
                                    propertyManager,
                                    instancesRepository,
                                    formsRepository,
                                    settingsProvider,
                                    instancesDataService,
                                    projectsDataService.requireCurrentProject().getUuid(),
                                    finalExternalUsername,
                                    finalExternalPassword,
                                    getString(org.odk.collect.strings.R.string.success),
                                    getString(org.odk.collect.strings.R.string.please_wait)
                            );
                        }
                        throw new IllegalArgumentException("Unknown ViewModel class");
                    }
                }
        ).get(InstanceUploadViewModel.class);

        instanceUploaderViewModel.getState().observe(this, state -> {
            if (state instanceof UploadState.AuthRequired) {
                authRequest(((UploadState.AuthRequired) state).getServer(), ((UploadState.AuthRequired) state).getResults());
            } else if (state instanceof UploadState.Progress) {
                progressUpdate(((UploadState.Progress) state).getCurrent(), ((UploadState.Progress) state).getTotal());
            } else if (state instanceof UploadState.Completed) {
                uploadingComplete(((UploadState.Completed) state).getResults());
            }
        });

        instanceUploaderViewModel.upload(Arrays.asList(instancesToSend));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isInstanceStateSaved = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        isInstanceStateSaved = true;
        super.onSaveInstanceState(outState);
        outState.putString(AUTH_URI, url);
        outState.putLongArray(TO_SEND, ArrayUtils.toPrimitive(instancesToSend));

        if (url != null) {
            outState.putString(ApplicationConstants.BundleKeys.URL, url);
        }
    }

    private void uploadingComplete(Map<String, String> result) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        // If the activity is paused or in the process of pausing, don't show the dialog
        if (!isInstanceStateSaved) {
            createUploadInstancesResultDialog(InstanceUploaderUtils.getUploadResultMessage(instancesRepository, this, result));
        } else {
            // Clean up
            finish();
        }
    }

    private void progressUpdate(int progress, int total) {
        instanceUploaderViewModel.setUploadingStatus(getString(org.odk.collect.strings.R.string.sending_items, String.valueOf(progress), String.valueOf(total)));
        progressDialog.setMessage(instanceUploaderViewModel.getUploadingStatus());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new DayNightProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                instanceUploaderViewModel.cancel();
                                finish();
                            }
                        };
                progressDialog.setTitle(getString(org.odk.collect.strings.R.string.uploading_data));
                progressDialog.setMessage(instanceUploaderViewModel.getUploadingStatus());
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setButton(getString(org.odk.collect.strings.R.string.cancel), loadingButtonListener);
                return progressDialog;
            case AUTH_DIALOG:
                AuthDialogUtility authDialogUtility = new AuthDialogUtility();
                if (instanceUploaderViewModel.getExternalUsername() != null && instanceUploaderViewModel.getExternalPassword() != null) {
                    authDialogUtility.setCustomUsername(instanceUploaderViewModel.getExternalUsername());
                    authDialogUtility.setCustomPassword(instanceUploaderViewModel.getExternalPassword());
                }
                return authDialogUtility.createDialog(this, this, this.url);
        }

        return null;
    }

    /**
     * Prompts the user for credentials for the server at the given URL. Once credentials are
     * provided, starts a new upload task with just the instances that were not yet reached.
     *
     * messagesByInstanceIdAttempted makes it possible to identify the instances that were part
     * of the latest submission attempt. The database provides generic status which could have come
     * from an unrelated submission attempt.
     */
    private void authRequest(Uri url, Map<String, String> messagesByInstanceIdAttempted) {
        if (progressDialog.isShowing()) {
            // should always be showing here
            progressDialog.dismiss();
        }

        // Remove sent instances from instances to send
        ArrayList<Long> workingSet = new ArrayList<>();
        Collections.addAll(workingSet, instancesToSend);
        if (messagesByInstanceIdAttempted != null) {
            Set<String> uploadedInstances = messagesByInstanceIdAttempted.keySet();

            for (String uploadedInstance : uploadedInstances) {
                Long removeMe = Long.valueOf(uploadedInstance);
                workingSet.remove(removeMe);
            }
        }

        // and reconstruct the pending set of instances to send
        Long[] updatedToSend = new Long[workingSet.size()];
        for (int i = 0; i < workingSet.size(); ++i) {
            updatedToSend[i] = workingSet.get(i);
        }
        instancesToSend = updatedToSend;

        this.url = url.toString();

        /** Once credentials are provided in the dialog, {@link #updatedCredentials()} is called */
        showDialog(AUTH_DIALOG);
    }

    private void createUploadInstancesResultDialog(String message) {
        String dialogTitle = getString(org.odk.collect.strings.R.string.upload_results);
        String buttonTitle = getString(org.odk.collect.strings.R.string.ok);

        SimpleDialog simpleDialog = SimpleDialog.newInstance(dialogTitle, 0, message, buttonTitle, true);
        simpleDialog.show(getSupportFragmentManager(), SimpleDialog.COLLECT_DIALOG_TAG);
    }

    @Override
    public void updatedCredentials() {
        showDialog(PROGRESS_DIALOG);
        instanceUploaderViewModel.upload(Arrays.asList(instancesToSend));
    }

    @Override
    public void cancelledUpdatingCredentials() {
        finish();
    }

    private String getReferrerUri() {
        Uri referrerUri = getReferrer();
        if (referrerUri != null) {
            return referrerUri.toString();
        }
        return "unknown";
    }
}
