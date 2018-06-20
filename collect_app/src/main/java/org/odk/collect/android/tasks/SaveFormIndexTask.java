/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import timber.log.Timber;

public class SaveFormIndexTask extends AsyncTask<Void, Void, String> {

    private final SaveFormIndexListener listener;
    private final FormIndex formIndex;

    public interface SaveFormIndexListener {
        void onSaveFormIndexError(String errorMessage);
    }

    public SaveFormIndexTask(SaveFormIndexListener listener, FormIndex formIndex) {
        this.listener = listener;
        this.formIndex = formIndex;
    }

    @Override
    protected String doInBackground(Void... params) {
        long start = System.currentTimeMillis();

        FormController formController = Collect.getInstance().getFormController();

        try {
            File tempFormIndexFile = SaveToDiskTask.getFormIndexFile(formController.getInstanceFile().getName());
            exportFormIndexToFile(formIndex, tempFormIndexFile);

            long end = System.currentTimeMillis();
            Timber.i("SaveFormIndex ms: %s to %s", Long.toString(end - start), tempFormIndexFile.toString());

            return null;
        } catch (Exception e) {
            String msg = e.getMessage();
            Timber.e(e);
            return msg;
        }
    }

    @Override
    protected void onPostExecute(String errorMessage) {
        super.onPostExecute(errorMessage);

        if (listener != null && errorMessage != null) {
            listener.onSaveFormIndexError(errorMessage);
        }
    }

    public static void exportFormIndexToFile(FormIndex formIndex, File savepointIndexFile) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savepointIndexFile));
            oos.writeObject(formIndex);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static FormIndex loadFormIndexFromFile() {
        try {
            String instanceName = Collect.getInstance()
                    .getFormController()
                    .getInstanceFile()
                    .getName();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SaveToDiskTask.getFormIndexFile(instanceName)));
            return (FormIndex) ois.readObject();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }
}
