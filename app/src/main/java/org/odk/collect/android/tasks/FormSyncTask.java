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

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.utilities.ChangeLockProvider;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;

/**
 * Background task for adding to the forms content provider, any forms that have been added to the
 * sdcard manually. Returns immediately if it detects an error.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormSyncTask extends AsyncTask<Void, String, String> {

    private final ChangeLockProvider changeLockProvider;
    private final String projectId;
    private DiskSyncListener listener;
    private String statusMessage = "";

    public FormSyncTask(ChangeLockProvider changeLockProvider, String projectId) {
        this.changeLockProvider = changeLockProvider;
        this.projectId = projectId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return changeLockProvider.getFormLock(projectId).withLock(acquiredLock -> {
            if (acquiredLock) {
                return new FormsDirDiskFormsSynchronizer().synchronizeAndReturnError();
            } else {
                return "";
            }
        });
    }

    public void setDiskSyncListener(DiskSyncListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // Make sure content observers (CursorLoaders for instance) are notified of change
        Collect.getInstance().getContentResolver().notifyChange(FormsContract.getUri(projectId), null);

        statusMessage = result;

        if (listener != null) {
            listener.syncComplete(result);
        }
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
