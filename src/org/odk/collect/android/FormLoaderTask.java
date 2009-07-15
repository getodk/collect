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

package org.odk.collect.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

import android.os.AsyncTask;

/**
 * Background task for loading a form.  Eventually we're moving this to a service so that
 * creating the formdef object happens automatically in order to dramatically speed form loading.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 *
 */
class FormLoaderTask extends AsyncTask<String, String, FormHandler> {
    FormLoaderListener mStateListener;


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected FormHandler doInBackground(String... path) {
        FormHandler fh = null;
        FormDef form = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        form = XFormUtils.getFormFromInputStream(fis);
        if (form == null) {
            return null;
        }

        fh = new FormHandler(form);
        return fh;
    }


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(FormHandler fh) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.loadingComplete(fh);
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        mStateListener = sl;
    }
}
