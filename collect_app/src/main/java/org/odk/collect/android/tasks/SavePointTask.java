/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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

import android.os.AsyncTask;

import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.listeners.SavePointListener;

import java.io.File;

import timber.log.Timber;

/**
 * Author: Meletis Margaritis
 * Date: 27/6/2013
 * Time: 6:46 μμ
 */
public class SavePointTask extends AsyncTask<Void, Void, String> {

    private static final Object LOCK = new Object();
    private static int lastPriorityUsed;

    private final SavePointListener listener;
    private final int priority;

    public SavePointTask(SavePointListener listener) {
        this.listener = listener;
        this.priority = ++lastPriorityUsed;
    }

    @Override
    protected String doInBackground(Void... params) {
        synchronized (LOCK) {
            if (priority < lastPriorityUsed) {
                Timber.w("Savepoint thread (p=%d) was cancelled (a) because another one is waiting (p=%d)", priority, lastPriorityUsed);
                return null;
            }

            long start = System.currentTimeMillis();

            try {
                FormController formController = Collect.getInstance().getFormController();
                File temp = SaveFormToDisk.getSavepointFile(formController.getInstanceFile().getName());
                ByteArrayPayload payload = formController.getFilledInFormXml();

                if (priority < lastPriorityUsed) {
                    Timber.w("Savepoint thread (p=%d) was cancelled (b) because another one is waiting (p=%d)", priority, lastPriorityUsed);
                    return null;
                }

                // write out xml
                SaveFormToDisk.writeFile(payload, temp.getAbsolutePath());

                long end = System.currentTimeMillis();
                Timber.i("Savepoint ms: %s to %s", Long.toString(end - start), temp.toString());

                return null;
            } catch (Exception e) {
                String msg = e.getMessage();
                Timber.e(e);
                return msg;
            }
        }
    }

    @Override
    protected void onPostExecute(String errorMessage) {
        super.onPostExecute(errorMessage);

        if (listener != null && errorMessage != null) {
            listener.onSavePointError(errorMessage);
        }
    }
}
