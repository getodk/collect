/*
 * Copyright (C) 2012 University of Washington
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

import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.forms.FormsRepository;

import timber.log.Timber;

/**
 * Task responsible for deleting selected instances.
 *
 * @author norman86@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class DeleteInstancesTask extends AsyncTask<Long, Integer, Integer> {

    private DeleteInstancesListener deleteInstancesListener;

    private int successCount;
    private int toDeleteCount;

    private final InstancesRepository instancesRepository;
    private final FormsRepository formsRepository;

    public DeleteInstancesTask(InstancesRepository instancesRepository, FormsRepository formsRepository) {
        this.instancesRepository = instancesRepository;
        this.formsRepository = formsRepository;
    }

    @Override
    protected Integer doInBackground(Long... params) {
        int deleted = 0;

        if (params == null) {
            return deleted;
        }

        toDeleteCount = params.length;

        InstanceDeleter instanceDeleter = new InstanceDeleter(instancesRepository, formsRepository);
        // delete files from database and then from file system
        for (Long param : params) {
            if (isCancelled()) {
                break;
            }
            try {
                instanceDeleter.delete(param);
                deleted++;

                successCount++;
                publishProgress(successCount, toDeleteCount);

            } catch (Exception ex) {
                Timber.e("Exception during delete of: %s exception: %s", param.toString(), ex.toString());
            }
        }
        successCount = deleted;
        return deleted;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (deleteInstancesListener != null) {
                deleteInstancesListener.progressUpdate(values[0], values[1]);
            }
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (deleteInstancesListener != null) {
            deleteInstancesListener.deleteComplete(result);
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        if (deleteInstancesListener != null) {
            deleteInstancesListener.deleteComplete(successCount);
        }
    }

    public void setDeleteListener(DeleteInstancesListener listener) {
        deleteInstancesListener = listener;
    }

    public int getDeleteCount() {
        return successCount;
    }

    public int getToDeleteCount() {
        return toDeleteCount;
    }
}
