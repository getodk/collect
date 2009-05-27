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

package org.google.android.odk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

import android.util.Log;


/**
 * A Serializable object that handles the loading of forms in a separate thread.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormLoader implements Serializable {

    private final static String t = "FormLoader";

    private static final long serialVersionUID = 1L;

    public enum LoadingState {
        NOT_RUNNING, RUNNING, FINISHED, ERROR
    }

    private LoadingState mLoadingState;
    private FormLoaderListener mStateListener;


    public FormLoader() {
        Log.i(t,"called constructor");

        mLoadingState = LoadingState.NOT_RUNNING;
        mStateListener = null;
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        mStateListener = sl;
    }


    /**
     * Loads the form specified by loadPath in a separate thread.
     * 
     * @param loadPath
     */
    public void loadForm(final String loadPath) {
        mLoadingState = LoadingState.RUNNING;
        new Thread() {
            @Override
            public void run() {
                FormDef form = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(loadPath));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                form = XFormUtils.getFormFromInputStream(fis);
                FormHandler fh = null;
                if (form == null) {
                    mLoadingState = LoadingState.ERROR;
                } else {
                    fh = new FormHandler(form);
                    mLoadingState = LoadingState.FINISHED;
                }
                
                // mStateListener can get set to null during an orientation change.
                // It is possible that the thread finishes after we've set the listener to null
                // and before the activity is restarted.  In that case the thread is restarted.
                if (mStateListener != null) {
                    mStateListener.loadingComplete(fh);
                } else {
                    mLoadingState = LoadingState.NOT_RUNNING;
                }
            }
        }.start();
    }
    

    public LoadingState getState() {
        return mLoadingState;
    }
}
