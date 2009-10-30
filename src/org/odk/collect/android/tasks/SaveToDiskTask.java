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

import org.javarosa.core.model.FormDef;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.FormHandler;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<FormHandler, String, Boolean> {
    FormSavedListener mSavedListener;
    String mInstancePath;
    Context mContext;
    Boolean mMarkCompleted;


    /**
     * Initialize {@link FormHandler} with {@link FormDef} from binary or from
     * XML. If given an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Boolean doInBackground(FormHandler... formhandler) {    
        FormHandler fh = formhandler[0];
        if (fh.exportData(mInstancePath, mContext, mMarkCompleted)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
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
}
