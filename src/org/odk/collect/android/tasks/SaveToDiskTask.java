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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {
    private final static String t = "SaveToDiskTask";

    private FormSavedListener mSavedListener;
    private String mInstanceDirPath;
    private String mDefaultUrl;
    private Context mContext;
    private Boolean mSave;
    private Boolean mMarkCompleted;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Integer doInBackground(Void... nothing) {

        // validation failed, pass specific failure
        int validateStatus = validateAnswers(mMarkCompleted);
        if (validateStatus != VALIDATED) {
            return validateStatus;
        }

        Collect.getInstance().getFormEntryController().getModel().getForm().postProcessInstance();

        if (mSave && exportData(mInstanceDirPath, mDefaultUrl, mContext, mMarkCompleted)) {
            return SAVED_AND_EXIT;
        } else if (exportData(mInstanceDirPath, mDefaultUrl, mContext, mMarkCompleted)) {
            return SAVED;
        }

        return SAVE_ERROR;

    }


    public boolean exportData(String instanceDirPath, String defaultUrl, Context context,
            boolean markCompleted) {

        Collect app = Collect.getInstance();

        File instanceDir = new File(instanceDirPath);
        File f = new File(FileUtils.getInstanceFilePath(instanceDirPath));

        ByteArrayPayload payload;
        try {

            // assume no binary data inside the model.
            FormInstance datamodel =
                app.getFormEntryController().getModel().getForm().getInstance();
            XFormSerializingVisitor serializer = new XFormSerializingVisitor();
            payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);

            // write out xml
            exportXmlFile(payload, f.getAbsolutePath());

        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }

        boolean canEditSubmission = true;
        String url = mDefaultUrl;
        {
            // now try to construct submission file
            File fSubmit = new File(FileUtils.getSubmissionBlobPath(instanceDirPath));
            try {
                // assume no binary data inside the model.
                FormEntryModel dataModel = app.getFormEntryController().getModel();
                FormDef formDef = dataModel.getForm();

                FormInstance formInstance = formDef.getInstance();

                IDataReference submissionElement = new XPathReference("/");
                // Determine the information about the submission...
                SubmissionProfile p = formDef.getSubmissionProfile();
                if (p != null) {
                    submissionElement = p.getRef();
                    String altUrl = p.getAction();
                    if (submissionElement == null || altUrl == null || !altUrl.startsWith("http")
                            || p.getMethod() == null || !p.getMethod().equals("form-data-post")) {
                        Log
                                .e(
                                    t,
                                    "Submission element should specify attributes: ref, method=\"form-data-post\", and action=\"http...\"");
                        return false;
                    }
                    url = altUrl;
                    TreeElement e = formInstance.resolveReference(new XPathReference("/"));
                    TreeElement ee = formInstance.resolveReference(submissionElement);
                    // we can edit the submission if the published fragment is the whole tree.
                    canEditSubmission = e.equals(ee);
                }

                if (mMarkCompleted) {
                    XFormSerializingVisitor serializer = new XFormSerializingVisitor();
                    payload =
                        (ByteArrayPayload) serializer.createSerializedPayload(formInstance,
                            submissionElement);

                    // write out xml
                    exportXmlFile(payload, fSubmit.getAbsolutePath());
                }

            } catch (IOException e) {
                Log.e(t, "Error creating serialized payload");
                e.printStackTrace();
                return false;
            }
        }

        boolean exists = false;
        long id = 0L;
        Cursor c = null;
        try {
            FilterUtils.FilterCriteria fd =
                FilterUtils.buildSelectionClause(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH,
                    instanceDir.getAbsolutePath());

            c = app.getContentResolver().query(SubmissionsStorage.CONTENT_URI_INFO_DATASET,
                    new String[] {
                        SubmissionsStorage.KEY_ID
                    }, fd.selection, fd.selectionArgs, null);
            if (c != null && c.moveToFirst()) {
                exists = true;
                id = c.getLong(c.getColumnIndex(SubmissionsStorage.KEY_ID));
            }
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        if (!mMarkCompleted) {
            url = null;
            if (!exists) {
                ContentValues values = new ContentValues();
                values.put(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH, instanceDir
                        .getAbsolutePath());
                values.put(SubmissionsStorage.KEY_STATUS, SubmissionsStorage.STATUS_INCOMPLETE);
                values.put(SubmissionsStorage.KEY_SUBMISSION_URI, url);
                values.put(SubmissionsStorage.KEY_CAN_EDIT_SUBMISSION, true);
                app.getContentResolver()
                        .insert(SubmissionsStorage.CONTENT_URI_INFO_DATASET, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(SubmissionsStorage.KEY_STATUS, SubmissionsStorage.STATUS_INCOMPLETE);
                values.put(SubmissionsStorage.KEY_SUBMISSION_URI, url);
                values.put(SubmissionsStorage.KEY_CAN_EDIT_SUBMISSION, true);
                app.getContentResolver().update(
                    ContentUris.withAppendedId(SubmissionsStorage.CONTENT_URI_INFO_DATASET, id),
                    values, null, null);
            }
        } else {
            if (!exists) {
                ContentValues values = new ContentValues();
                values.put(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH, instanceDir
                        .getAbsolutePath());
                values.put(SubmissionsStorage.KEY_STATUS, SubmissionsStorage.STATUS_COMPLETE);
                values.put(SubmissionsStorage.KEY_DISPLAY_SUB_SUBTEXT, app
                        .getString(R.string.will_be_sent_to)
                        + url);
                values.put(SubmissionsStorage.KEY_SUBMISSION_URI, url);
                values.put(SubmissionsStorage.KEY_CAN_EDIT_SUBMISSION, canEditSubmission);
                app.getContentResolver()
                        .insert(SubmissionsStorage.CONTENT_URI_INFO_DATASET, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(SubmissionsStorage.KEY_STATUS, SubmissionsStorage.STATUS_COMPLETE);
                values.put(SubmissionsStorage.KEY_DISPLAY_SUB_SUBTEXT, app
                        .getString(R.string.will_be_sent_to)
                        + url);
                values.put(SubmissionsStorage.KEY_SUBMISSION_URI, url);
                values.put(SubmissionsStorage.KEY_CAN_EDIT_SUBMISSION, canEditSubmission);
                app.getContentResolver().update(
                    ContentUris.withAppendedId(SubmissionsStorage.CONTENT_URI_INFO_DATASET, id),
                    values, null, null);
            }
        }
        return true;

    }


    private boolean exportXmlFile(ByteArrayPayload payload, String path) {

        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        try {
            int read = is.read(data, 0, len);
            if (read > 0) {
                // write xml file
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
                    bw.write(new String(data, "UTF-8"));
                    bw.flush();
                    bw.close();
                    return true;

                } catch (IOException e) {
                    Log.e(t, "Error writing XML file");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(t, "Error reading from payload data stream");
            e.printStackTrace();
            return false;
        }

        return false;

    }


    @Override
    protected void onPostExecute(Integer result) {
        synchronized (this) {
            if (mSavedListener != null)
                mSavedListener.savingComplete(result);
        }
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }


    public void setExportVars(String instanceDirPath, String defaultUrl, Context context,
            Boolean saveAndExit, Boolean markCompleted) {
        mInstanceDirPath = instanceDirPath;
        mDefaultUrl = defaultUrl;
        mContext = context;
        mSave = saveAndExit;
        mMarkCompleted = markCompleted;
    }


    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     * 
     * @param markCompleted
     * @return validatedStatus
     */

    private int validateAnswers(Boolean markCompleted) {
        FormEntryController fec = Collect.getInstance().getFormEntryController();
        FormEntryModel fem = fec.getModel();
        FormIndex i = fem.getFormIndex();

        fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event = fec.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {
            if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                int saveStatus = fec.answerQuestion(fem.getQuestionPrompt().getAnswerValue());
                if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {
                    this.publishProgress(fem.getQuestionPrompt().getConstraintText(), Integer
                            .toString(saveStatus));
                    return saveStatus;
                }
            }
        }

        fec.jumpToIndex(i);
        return VALIDATED;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        Collect.getInstance().createConstraintToast(values[0],
            Integer.valueOf(values[1]).intValue());
    }

}
