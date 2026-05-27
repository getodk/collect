/*
 * Copyright 2018 Nafundi
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
package org.odk.collect.android.preferences.screens

import android.os.Bundle
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog.MovingBackwardsDialogListener
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog.ResetSettingsResultDialogListener
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.dialogs.DeleteProjectDialog
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class ProjectPreferencesActivity :
    LocalizedActivity(),
    ResetSettingsResultDialogListener,
    MovingBackwardsDialogListener {

    private var isInstanceStateSaved = false

    @Inject
    lateinit var propertyManager: PropertyManager

    @Inject
    lateinit var projectDeleter: ProjectDeleter

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var instancesDataService: InstancesDataService

    @Inject
    lateinit var scheduler: Scheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(ProjectPreferencesFragment::class.java) {
                ProjectPreferencesFragment(intent.getBooleanExtra(EXTRA_IN_FORM_ENTRY, false))
            }
            .forClass(DeleteProjectDialog::class) {
                DeleteProjectDialog(
                    projectDeleter,
                    projectsDataService,
                    formsDataService,
                    instancesDataService,
                    scheduler
                )
            }
            .build()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences_layout)
    }

    override fun onPause() {
        super.onPause()
        propertyManager.reload()
    }

    override fun onPostResume() {
        super.onPostResume()
        isInstanceStateSaved = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        isInstanceStateSaved = true
        super.onSaveInstanceState(outState)
    }

    override fun onDialogClosed() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
    }

    override fun preventOtherWaysOfEditingForm() {
        val fragment = supportFragmentManager.findFragmentById(R.id.preferences_fragment_container) as FormEntryAccessPreferencesFragment
        fragment.preventOtherWaysOfEditingForm()
    }

    fun isInstanceStateSaved() = isInstanceStateSaved

    companion object {
        const val EXTRA_IN_FORM_ENTRY = "in_form_entry"
    }
}
