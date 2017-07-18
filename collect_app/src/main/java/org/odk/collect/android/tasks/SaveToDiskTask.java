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

package org.odk.collect.android.tasks;

import java.util.Calendar;
import java.util.Date;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.EncryptionException;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.EncryptionUtils;
import org.odk.collect.android.utilities.EncryptionUtils.EncryptedFormInformation;
import org.odk.collect.android.utilities.FileUtils;

import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import timber.log.Timber;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<Void, String, SaveResult> {

    private FormSavedListener savedListener;
    private Boolean save;
    private Boolean markCompleted;
    private Uri uri;
    private String instanceName;
    private long mTaskId;		    // ---------- SMAP
    private String mFormPath;	    // ---------- SMAP
    private String mSurveyNotes;	// ---------- SMAP
    private boolean mCanUpdate = true;  // Smap

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;
    public static final int ENCRYPTION_ERROR = 505;

    public SaveToDiskTask(Uri uri, Boolean saveAndExit, Boolean markCompleted, String updatedName,
    		long taskId, String formPath, String surveyNotes, boolean canUpdate) {		// SMAP added assignment_id, formPath
        this.uri = uri;
        save = saveAndExit;
        this.markCompleted = markCompleted;
        instanceName = updatedName;
        mTaskId = taskId;  // SMAP
        mFormPath = formPath; // SMAP
        mSurveyNotes = surveyNotes; // Smap
        mCanUpdate = canUpdate; // Smap
    }


    /**
     * Initialize {@link FormEntryController} with {@link org.javarosa.core.model.FormDef} from binary or from XML. If
     * given
     * an instance, it will be used to fill the {@link org.javarosa.core.model.FormDef}.
     */
    @Override
    protected SaveResult doInBackground(Void... nothing) {
        SaveResult saveResult = new SaveResult();

        FormController formController = Collect.getInstance().getFormController();

        publishProgress(Collect.getInstance().getString(R.string.survey_saving_validating_message));

        try {
            int validateStatus = formController.validateAnswers(markCompleted);
            if (validateStatus != FormEntryController.ANSWER_OK) {
                // validation failed, pass specific failure
                saveResult.setSaveResult(validateStatus, markCompleted);
                return saveResult;
            }
        } catch (Exception e) {
            Timber.e(e);

            // SCTO-825
            // that means that we have a bad design
            // save the exception to be used in the error dialog.
            saveResult.setSaveErrorMessage(e.getMessage());
            saveResult.setSaveResult(SAVE_ERROR, markCompleted);
            return saveResult;
        }

        // check if the "Cancel" was hit and exit.
        if (isCancelled()) {
            return null;
        }

        if (markCompleted) {
            formController.postProcessInstance();
        }

        Collect.getInstance().getActivityLogger().logInstanceAction(this, "save",
                Boolean.toString(markCompleted));

        // close all open databases of external data.
        Collect.getInstance().getExternalDataManager().close();

        // if there is a meta/instanceName field, be sure we are using the latest value
        // just in case the validate somehow triggered an update.
        String updatedSaveName = formController.getSubmissionMetadata().instanceName;
        if (updatedSaveName != null) {
            instanceName = updatedSaveName;
        }

        try {
    	    exportData(markCompleted, canUpdate);     // smap

            // attempt to remove any scratch file
            File shadowInstance = savepointFile(formController.getInstancePath());
            if (shadowInstance.exists()) {
                FileUtils.deleteAndReport(shadowInstance);
            }

            saveResult.setSaveResult(save ? SAVED_AND_EXIT : SAVED, markCompleted);
        } catch (EncryptionException e) {
            saveResult.setSaveErrorMessage(e.getMessage());
            saveResult.setSaveResult(ENCRYPTION_ERROR, markCompleted);
        } catch (Exception e) {
            Timber.e(e);

            saveResult.setSaveErrorMessage(e.getMessage());
            saveResult.setSaveResult(SAVE_ERROR, markCompleted);
        }

        return saveResult;
    }

    private void updateInstanceDatabase(boolean incomplete, boolean canEditAfterCompleted, boolean canUpdate) {     // smap

    	String source = null;       // smap
        FormController formController = Collect.getInstance().getFormController();

        // Update the instance database...
        ContentValues values = new ContentValues();
        if (canUpdate && mInstanceName != null) {       // smap
            values.put(InstanceColumns.DISPLAY_NAME, instanceName);
        }
        if(canUpdate) {
            if (incomplete || !markCompleted) {
                values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
            } else {
                values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_COMPLETE);
            }
        }

        // Smap Start
        if(canUpdate) {
            if (markCompleted) {
                values.put(InstanceColumns.T_TASK_STATUS, "complete");
            } else {
                values.put(InstanceColumns.T_TASK_STATUS, "accepted");
            }
        }

        // Add uuid
        if(canUpdate) {
            values.put(InstanceColumns.UUID, formController.getSubmissionMetadata().instanceId);
        }

        // Add actual location
        if(canUpdate) {
            Location location = Collect.getInstance().getLocation();
            double lon = 0.0;
            double lat = 0.0;
            if (location != null) {
                Log.i("saveToDiskTask", "Setting location");
                lon = location.getLongitude();
                lat = location.getLatitude();
            } else {
                Log.i("saveToDiskTask", "Location is null");
            }
            values.put(InstanceColumns.ACT_LON, lon);
            values.put(InstanceColumns.ACT_LAT, lat);

            values.put(InstanceColumns.T_ACT_FINISH, Calendar.getInstance().getTime().getTime());
            values.put(InstanceColumns.T_IS_SYNC, InstanceProviderAPI.STATUS_SYNC_NO);
        }
        values.put(InstanceColumns.T_SURVEY_NOTES, mSurveyNotes);
        // Smap End

        // update this whether or not the status is complete...
        values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(canEditAfterCompleted));

        // If FormEntryActivity was started with an Instance, just update that instance
        if (Collect.getInstance().getContentResolver().getType(uri).equals(
                InstanceColumns.CONTENT_ITEM_TYPE)) {
            int updated = Collect.getInstance().getContentResolver().update(uri, values, null,
                    null);
            if (updated > 1) {
                Timber.w("Updated more than one entry, that's not good: %s", uri.toString());
            } else if (updated == 1) {
                Timber.i("Instance successfully updated");
            } else {
                Timber.e("Instance doesn't exist but we have its Uri!! %s", uri.toString());
            }
        } else if (Collect.getInstance().getContentResolver().getType(uri).equals(
                FormsColumns.CONTENT_ITEM_TYPE)) {
            // If FormEntryActivity was started with a form, then it's likely the first time we're
            // saving.
            // However, it could be a not-first time saving if the user has been using the manual
            // 'save data' option from the menu. So try to update first, then make a new one if that
            // fails.
            String instancePath = formController.getInstancePath().getAbsolutePath();
            String where = InstanceColumns.INSTANCE_FILE_PATH + "=?";
            String[] whereArgs = {
                    instancePath
            };
            int updated = new InstancesDao().updateInstance(values, where, whereArgs);
            if (updated > 1) {
                Timber.w("Updated more than one entry, that's not good: %s", instancePath);
            } else if (updated == 1) {
                Timber.i("Instance found and successfully updated: %s", instancePath);
                // already existed and updated just fine
            } else {
                Timber.i("No instance found, creating");
                // Entry didn't exist, so create it.
                Cursor c = null;
                try {
                	// retrieve the form definition...
                	c = Collect.getInstance().getContentResolver().query(mUri, null, null, null, null);
	                c.moveToFirst();
	                source = c.getString(c.getColumnIndex(FormsColumns.SOURCE));				// smap
	                String formname = c.getString(c.getColumnIndex(FormsColumns.DISPLAY_NAME));
	                String submissionUri = null;
	                if ( !c.isNull(c.getColumnIndex(FormsColumns.SUBMISSION_URI)) ) {
	                	submissionUri = c.getString(c.getColumnIndex(FormsColumns.SUBMISSION_URI));
	                }

	                // add missing fields into values
	                values.put(InstanceColumns.INSTANCE_FILE_PATH, instancePath);
	                values.put(InstanceColumns.SUBMISSION_URI, submissionUri);
	                if (instanceName != null) {
	                    values.put(InstanceColumns.DISPLAY_NAME, instanceName);
	                } else {
	                    values.put(InstanceColumns.DISPLAY_NAME, formname);
	                }

                    // Smap Start
                    values.put(InstanceColumns.SOURCE, source);
                    if (mInstanceName != null) {
                        values.put(InstanceColumns.T_TITLE, mInstanceName);
                    } else {
                        values.put(InstanceColumns.T_TITLE, formname);
                    }
                    String jrformid = c.getString(c.getColumnIndex(FormsColumns.JR_FORM_ID));
                    String jrversion = c.getString(c.getColumnIndex(FormsColumns.JR_VERSION));
                    values.put(InstanceColumns.JR_FORM_ID, jrformid);
                    values.put(InstanceColumns.JR_VERSION, jrversion);

                    // Smap End

                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
                uri = new InstancesDao().saveInstance(values);
            }
        }

        Intent intent = new Intent("refresh");      // Smap
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent); // Smap
    }

    /**
     * Return the name of the savepoint file for a given instance.
     */
    public static File savepointFile(File instancePath) {
        File tempDir = new File(Collect.CACHE_PATH);
        return new File(tempDir, instancePath.getName() + ".save");
    }

    /**
     * Write's the data to the sdcard, and updates the instances content provider.
     * In theory we don't have to write to disk, and this is where you'd add
     * other methods.
     */
    private void exportData(boolean markCompleted, boolean canUpdate) throws IOException, EncryptionException {     // smap
        FormController formController = Collect.getInstance().getFormController();

        publishProgress(Collect.getInstance().getString(R.string.survey_saving_collecting_message));

        ByteArrayPayload payload = formController.getFilledInFormXml();
        // write out xml
        String instancePath = formController.getInstancePath().getAbsolutePath();

        publishProgress(Collect.getInstance().getString(R.string.survey_saving_saving_message));
        if(canUpdate) {      // smap
            exportXmlFile(payload, instancePath);
        }

        // update the uri. We have exported the reloadable instance, so update status...
        // Since we saved a reloadable instance, it is flagged as re-openable so that if any error
        // occurs during the packaging of the data for the server fails (e.g., encryption),
        // we can still reopen the filled-out form and re-save it at a later time.
        updateInstanceDatabase(true, true, canUpdate);      // smap

        if ( markCompleted && canUpdate ) {     // smap
            // now see if the packaging of the data for the server would make it
            // non-reopenable (e.g., encryption or send an SMS or other fraction of the form).
            boolean canEditAfterCompleted = formController.isSubmissionEntireForm();
            boolean isEncrypted = false;

            // build a submission.xml to hold the data being submitted
            // and (if appropriate) encrypt the files on the side

            // pay attention to the ref attribute of the submission profile...
            File instanceXml = formController.getInstancePath();
            File submissionXml = new File(instanceXml.getParentFile(), "submission.xml");

            payload = formController.getSubmissionXml();

            // write out submission.xml -- the data to actually submit to aggregate

            publishProgress(
                    Collect.getInstance().getString(R.string.survey_saving_finalizing_message));

            exportXmlFile(payload, submissionXml.getAbsolutePath());

            // see if the form is encrypted and we can encrypt it...
            EncryptedFormInformation formInfo = EncryptionUtils.getEncryptedFormInformation(uri,
                    formController.getSubmissionMetadata());
            if (formInfo != null) {
                // if we are encrypting, the form cannot be reopened afterward
                canEditAfterCompleted = false;
                // and encrypt the submission (this is a one-way operation)...

                publishProgress(
                        Collect.getInstance().getString(R.string.survey_saving_encrypting_message));

                EncryptionUtils.generateEncryptedSubmission(instanceXml, submissionXml, formInfo);
                isEncrypted = true;
            }

            // At this point, we have:
            // 1. the saved original instanceXml,
            // 2. all the plaintext attachments
            // 2. the submission.xml that is the completed xml (whether encrypting or not)
            // 3. all the encrypted attachments if encrypting (isEncrypted = true).
            //
            // NEXT:
            // 1. Update the instance database (with status complete).
            // 2. Overwrite the instanceXml with the submission.xml
            //    and remove the plaintext attachments if encrypting

            updateInstanceDatabase(false, canEditAfterCompleted, canUpdate);    // smap

            if (!canEditAfterCompleted) {
                // AT THIS POINT, there is no going back.  We are committed
                // to returning "success" (true) whether or not we can
                // rename "submission.xml" to instanceXml and whether or
                // not we can delete the plaintext media files.
                //
                // Handle the fall-out for a failed "submission.xml" rename
                // in the InstanceUploader task.  Leftover plaintext media
                // files are handled during form deletion.

                // delete the restore Xml file.
                if (!instanceXml.delete()) {
                    String msg = "Error deleting " + instanceXml.getAbsolutePath()
                            + " prior to renaming submission.xml";
                    Timber.e(msg);
                    throw new IOException(msg);
                }

                // rename the submission.xml to be the instanceXml
                if (!submissionXml.renameTo(instanceXml)) {
                    String msg =
                            "Error renaming submission.xml to " + instanceXml.getAbsolutePath();
                    Timber.e(msg);
                    throw new IOException(msg);
                }
            } else {
                // try to delete the submissionXml file, since it is
                // identical to the existing instanceXml file
                // (we don't need to delete and rename anything).
                if (!submissionXml.delete()) {
                    String msg = "Error deleting " + submissionXml.getAbsolutePath()
                            + " (instance is re-openable)";
                    Timber.w(msg);
                }
            }

            // if encrypted, delete all plaintext files
            // (anything not named instanceXml or anything not ending in .enc)
            if (isEncrypted) {
                if (!EncryptionUtils.deletePlaintextFiles(instanceXml)) {
                    Timber.e("Error deleting plaintext files for %s", instanceXml.getAbsolutePath());
                }
            }
        }
    }


    /**
     * This method actually writes the xml to disk.
     */
    static void exportXmlFile(ByteArrayPayload payload, String path) throws IOException {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot overwrite " + path + ". Perhaps the file is locked?");
        }

        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        // try {
        int read = is.read(data, 0, len);
        if (read > 0) {
            // write xml file
            RandomAccessFile randomAccessFile = null;
            try {
                // String filename = path + File.separator +
                // path.substring(path.lastIndexOf(File.separator) + 1) + ".xml";
                randomAccessFile = new RandomAccessFile(file, "rws");
                randomAccessFile.write(data);
            } finally {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        Timber.e(e, "Error closing RandomAccessFile: %s", path);
                    }
                }
            }
        }
        //        } catch (IOException e) {
        //            Log.e(t, "Error reading from payload data stream");
        //            e.printStackTrace();
        //            return false;
        //        }
        //
        //        return false;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        if (savedListener != null && values != null) {
            if (values.length == 1) {
                savedListener.onProgressStep(values[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(SaveResult result) {
        synchronized (this) {
            if (savedListener != null && result != null) {
                savedListener.savingComplete(result, mTaskId);		// smap added mTaskId
            }
        }
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            savedListener = fsl;
        }
    }


}
