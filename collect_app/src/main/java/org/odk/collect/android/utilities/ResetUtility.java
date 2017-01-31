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
package org.odk.collect.android.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.DatabaseReader;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DeleteInstancesTask;

import java.io.File;

public class ResetUtility {

    public void reset(final Context context, boolean resetPreferences, boolean resetInstances,
                      boolean resetForms, boolean resetLayers, boolean resetDatabases) {
        if (resetPreferences) {
            resetPreferences(context);
        }
        if (resetInstances) {
            resetInstances(context);
        }
        if (resetForms) {
            resetForms(context);
        }
        if (resetLayers) {
            deleteFolderContents(Collect.OFFLINE_LAYERS);
        }
        if (resetDatabases) {
            context.getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
            context.getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);        }
    }

    private void resetPreferences(Context context) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .apply();

        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
    }

    private void resetInstances(final Context context) {
        final Long[] allInstances = new DatabaseReader().getAllInstancesIDs(context);

        DeleteInstancesTask task = new DeleteInstancesTask();
        task.setContentResolver(context.getContentResolver());
        task.execute(allInstances);
    }

    private void resetForms(final Context context) {
        final Long[] allForms = new DatabaseReader().getAllFormsIDs(context);

        DeleteFormsTask task = new DeleteFormsTask();
        task.setContentResolver(context.getContentResolver());
        task.execute(allForms);
    }

    private void deleteFolderContents(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();

            for (File f : files) {
                deleteRecursive(f);
            }
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}