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

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ApplicationResetter {

    private List<Integer> failedResetActions;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    PropertyManager propertyManager;

    @Inject
    ServerRepository serverRepository;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    InstancesRepository instancesRepository;

    @Inject
    FormsRepository formsRepository;

    public ApplicationResetter() {
        // This should probably just take arguments in the constructor rather than use Dagger
        DaggerUtils.getComponent(Collect.getInstance()).inject(this);
    }

    public List<Integer> reset(List<Integer> resetActions) {
        failedResetActions = new ArrayList<>();
        failedResetActions.addAll(resetActions);

        for (int action : resetActions) {
            switch (action) {
                case ResetAction.RESET_PREFERENCES:
                    resetPreferences();
                    serverRepository.clear();
                    break;
                case ResetAction.RESET_INSTANCES:
                    resetInstances();
                    break;
                case ResetAction.RESET_FORMS:
                    resetForms();
                    break;
                case ResetAction.RESET_LAYERS:
                    if (deleteFolderContents(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS))) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_LAYERS));
                    }
                    break;
                case ResetAction.RESET_CACHE:
                    if (deleteFolderContents(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE))) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_CACHE));
                    }
                    break;
                case ResetAction.RESET_OSM_DROID:
                    if (deleteFolderContents(Configuration.getInstance().getOsmdroidTileCache().getPath())) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_OSM_DROID));
                    }
                    break;
            }
        }

        return failedResetActions;
    }

    private void resetPreferences() {
        WebCredentialsUtils.clearAllCredentials();

        settingsProvider.getGeneralSettings().clear();
        settingsProvider.getGeneralSettings().setDefaultForAllSettingsWithoutValues();
        settingsProvider.getAdminSettings().clear();
        settingsProvider.getAdminSettings().setDefaultForAllSettingsWithoutValues();

        boolean deletedSettingsFolderContest = !new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS)).exists()
                || deleteFolderContents(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS));

        if (deletedSettingsFolderContest) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_PREFERENCES));
        }

        propertyManager.reload();
    }

    private void resetInstances() {
        instancesRepository.deleteAll();

        if (deleteFolderContents(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES))) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_INSTANCES));
        }
    }

    private void resetForms() {
        formsRepository.deleteAll();

        File itemsetDbFile = new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + File.separator + ItemsetDbAdapter.DATABASE_NAME);

        if (deleteFolderContents(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS)) && (!itemsetDbFile.exists() || itemsetDbFile.delete())) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_FORMS));
        }
    }

    private boolean deleteFolderContents(String path) {
        boolean result = true;
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result = deleteRecursive(f);
                }
            }
        }
        return result;
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    public static class ResetAction {
        public static final int RESET_PREFERENCES = 0;
        public static final int RESET_INSTANCES = 1;
        public static final int RESET_FORMS = 2;
        public static final int RESET_LAYERS = 3;
        public static final int RESET_CACHE = 4;
        public static final int RESET_OSM_DROID = 5;
    }
}
