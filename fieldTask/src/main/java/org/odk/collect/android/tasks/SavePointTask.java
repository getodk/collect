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
import android.util.Log;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.SavePointListener;
import org.odk.collect.android.logic.FormController;

import java.io.File;

/**
 * Author: Meletis Margaritis
 * Date: 27/6/2013
 * Time: 6:46 μμ
 */
public class SavePointTask extends AsyncTask<Void, Void, String> {

    private final static String t = "SavePointTask";
    private static final Object lock = new Object();
    private static int lastPriorityUsed = 0;

    private final SavePointListener listener;
    private int priority;

    public SavePointTask(SavePointListener listener) {
        this.listener = listener;
        this.priority = ++lastPriorityUsed;
    }

    @Override
    protected String doInBackground(Void... params) {
        synchronized (lock) {
            if (priority < lastPriorityUsed) {
                Log.w(t, "Savepoint thread (p=" + priority + ") was cancelled (a) because another one is waiting (p=" + lastPriorityUsed + ")");
                return null;
            }

            long start = System.currentTimeMillis();

            try {
                FormController formController = Collect.getInstance().getFormController();
                File temp = SaveToDiskTask.savepointFile(formController.getInstancePath());
                ByteArrayPayload payload = formController.getFilledInFormXml();

                if (priority < lastPriorityUsed) {
                    Log.w(t, "Savepoint thread (p=" + priority + ") was cancelled (b) because another one is waiting (p=" + lastPriorityUsed + ")");
                    return null;
                }

                // write out xml
                SaveToDiskTask.exportXmlFile(payload, temp.getAbsolutePath());

                long end = System.currentTimeMillis();
                Log.i(t, "Savepoint ms: " + Long.toString(end - start) + " to " + temp);

                return null;
            } catch (Exception e) {
                String msg = e.getMessage();
                Log.e(t, msg, e);
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
