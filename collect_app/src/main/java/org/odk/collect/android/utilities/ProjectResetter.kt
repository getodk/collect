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
package org.odk.collect.android.utilities

import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.logic.PropertyManager
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.settings.SettingsProvider
import java.io.File

class ProjectResetter(
    private val storagePathProvider: StoragePathProvider,
    private val propertyManager: PropertyManager,
    private val settingsProvider: SettingsProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider
) {

    private var failedResetActions = mutableListOf<Int>()

    fun reset(resetActions: List<Int>): List<Int> {
        for (action in resetActions) {
            when (action) {
                ResetAction.RESET_PREFERENCES -> resetPreferences()
                ResetAction.RESET_INSTANCES -> resetInstances()
                ResetAction.RESET_FORMS -> resetForms()
                ResetAction.RESET_LAYERS -> resetLayers()
                ResetAction.RESET_CACHE -> resetCache()
            }
        }
        return failedResetActions
    }

    private fun resetPreferences() {
        WebCredentialsUtils.clearAllCredentials()

        settingsProvider.getUnprotectedSettings().clear()
        settingsProvider.getUnprotectedSettings().setDefaultForAllSettingsWithoutValues()
        settingsProvider.getProtectedSettings().clear()
        settingsProvider.getProtectedSettings().setDefaultForAllSettingsWithoutValues()

        if (!deleteFolderContent(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS))) {
            failedResetActions.add(ResetAction.RESET_PREFERENCES)
        }

        propertyManager.reload()
    }

    private fun resetInstances() {
        instancesRepositoryProvider.get().deleteAll()

        if (!deleteFolderContent(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES))) {
            failedResetActions.add(ResetAction.RESET_INSTANCES)
        }
    }

    private fun resetForms() {
        formsRepositoryProvider.get().deleteAll()

        File(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA) + File.separator + ItemsetDbAdapter.DATABASE_NAME).delete()

        if (!deleteFolderContent(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS))) {
            failedResetActions.add(ResetAction.RESET_FORMS)
        }
    }

    private fun resetLayers() {
        if (!deleteFolderContent(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS))) {
            failedResetActions.add(ResetAction.RESET_LAYERS)
        }
    }

    private fun resetCache() {
        if (!deleteFolderContent(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE))) {
            failedResetActions.add(ResetAction.RESET_CACHE)
        }
    }

    private fun deleteFolderContent(path: String): Boolean {
        var result = true
        val file = File(path)
        if (file.exists()) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    result = f.deleteRecursively()
                }
            }
        }
        return result
    }

    object ResetAction {
        const val RESET_PREFERENCES = 0
        const val RESET_INSTANCES = 1
        const val RESET_FORMS = 2
        const val RESET_LAYERS = 3
        const val RESET_CACHE = 4
    }
}
