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

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import timber.log.Timber;

/**
 * Task responsible for deleting selected instances.
 *
 * @author norman86@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class DeleteInstancesTask extends AsyncTask<Long, Integer, Integer> {

    private ContentResolver contentResolver;
    private DeleteInstancesListener deleteInstancesListener;

    private int successCount;
    private int toDeleteCount;

    @Override
    protected Integer doInBackground(Long... params) {
        int deleted = 0;

        if (params == null || contentResolver == null) {
            return deleted;
        }

        toDeleteCount = params.length;

        // delete files from database and then from file system
        for (Long param : params) {
            if (isCancelled()) {
                break;
            }
            try {
                Uri deleteForm =
                        Uri.withAppendedPath(InstanceColumns.CONTENT_URI, param.toString());

                int wasDeleted = contentResolver.delete(deleteForm, null, null);
                deleted += wasDeleted;

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
        contentResolver = null;
        if (deleteInstancesListener != null) {
            deleteInstancesListener.deleteComplete(result);
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        contentResolver = null;
        if (deleteInstancesListener != null) {
            deleteInstancesListener.deleteComplete(successCount);
        }
    }

    public void setDeleteListener(DeleteInstancesListener listener) {
        deleteInstancesListener = listener;
    }

    public void setContentResolver(ContentResolver resolver) {
        contentResolver = resolver;
    }

    public int getDeleteCount() {
        return successCount;
    }

    public int getToDeleteCount() {
        return toDeleteCount;
    }
}
