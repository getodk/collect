/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.FormHandler;
import org.odk.collect.android.logic.GlobalConstants;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {

    private FormSavedListener mSavedListener;
    private String mInstancePath;
    private Context mContext;
    private Boolean mMarkCompleted;
    private FormHandler mFormHandler = FormEntryActivity.mFormHandler;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;


    /**
     * Initialize {@link FormHandler} with {@link FormDef} from binary or from
     * XML. If given an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Integer doInBackground(Void... nothing) {
        int validateStatus = validateAnswers(mMarkCompleted);
        if (validateStatus != VALIDATED) {
            return validateStatus;
        }

        mFormHandler.postProcessForm();
        if (mFormHandler.exportData(mInstancePath, mContext, mMarkCompleted)) {
            return SAVED;
        }
        return SAVE_ERROR;
    }


    @Override
    protected void onPostExecute(Integer result) {
        synchronized (this) {
            if (mSavedListener != null) mSavedListener.savingComplete(result);
        }
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }


    public void setExportVars(String instancePath, Context context, Boolean completed) {
        mInstancePath = instancePath;
        mContext = context;
        mMarkCompleted = completed;
    }


    // make sure this validates for all on done
    private int validateAnswers(boolean markCompleted) {
        mFormHandler.setFormIndex(FormIndex.createBeginningOfFormIndex());
        mFormHandler.nextQuestionPrompt();
        while (!mFormHandler.isEnd()) {
            int saveStatus =
                    mFormHandler.saveAnswer(mFormHandler.currentPrompt(), mFormHandler
                            .currentPrompt().getAnswerValue(), true);
            if (saveStatus == GlobalConstants.ANSWER_CONSTRAINT_VIOLATED
                    || (markCompleted && saveStatus != GlobalConstants.ANSWER_OK)) {
                return saveStatus;
            }
            mFormHandler.nextQuestionPrompt();
        }

        return VALIDATED;
    }
}
