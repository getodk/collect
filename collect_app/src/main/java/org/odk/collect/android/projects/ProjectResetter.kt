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
package org.odk.collect.android.projects

import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.settings.SettingsProvider
import java.io.File

class ProjectResetter(
    storagePathProvider: ProjectDependencyFactory<StoragePaths>,
    private val propertyManager: PropertyManager,
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: ProjectDependencyFactory<FormsRepository>,
    private val savepointsRepositoryProvider: ProjectDependencyFactory<SavepointsRepository>,
    private val instancesDataService: InstancesDataService,
    private val projectId: String
) {
    private val storagePaths = storagePathProvider.create(projectId)

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

        if (!deleteFolderContent(storagePaths.settingsDir)) {
            failedResetActions.add(ResetAction.RESET_PREFERENCES)
        }

        propertyManager.reload()
    }

    private fun resetInstances() {
        if (instancesDataService.reset(projectId)) {
            savepointsRepositoryProvider.create(projectId).deleteAll()
        } else {
            failedResetActions.add(ResetAction.RESET_INSTANCES)
        }
    }

    private fun resetForms() {
        formsRepositoryProvider.create(projectId).deleteAll()

        File(storagePaths.metaDir + File.separator + ItemsetDbAdapter.DATABASE_NAME).delete()

        if (!deleteFolderContent(storagePaths.formsDir)) {
            failedResetActions.add(ResetAction.RESET_FORMS)
        }
    }

    private fun resetLayers() {
        if (!deleteFolderContent(storagePaths.layersDir)) {
            failedResetActions.add(ResetAction.RESET_LAYERS)
        }
    }

    private fun resetCache() {
        savepointsRepositoryProvider.create(projectId).deleteAll()
        if (!deleteFolderContent(storagePaths.cacheDir)) {
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
