/*
 * Copyright (C) 2017 Shobhit
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
package org.odk.collect.android.preferences.screens

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.activities.SplashScreenActivity
import org.odk.collect.android.configure.qr.QRCodeTabsActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.ResetDialogPreference
import org.odk.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.projects.DeleteProjectResult.DeletedSuccessfully
import org.odk.collect.android.projects.DeleteProjectResult.RunningBackgroundJobs
import org.odk.collect.android.projects.DeleteProjectResult.UnsentInstances
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.android.utilities.ToastUtils.showLongToast
import javax.inject.Inject

class AdminPreferencesFragment :
    BaseAdminPreferencesFragment(), Preference.OnPreferenceClickListener {

    @Inject
    lateinit var projectDeleter: ProjectDeleter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.admin_preferences, rootKey)
        findPreference<Preference>("odk_preferences")!!.onPreferenceClickListener = this
        findPreference<Preference>(AdminKeys.KEY_IMPORT_SETTINGS)!!.onPreferenceClickListener = this
        findPreference<Preference>("main_menu")!!.onPreferenceClickListener = this
        findPreference<Preference>("user_settings")!!.onPreferenceClickListener = this
        findPreference<Preference>("form_entry")!!.onPreferenceClickListener = this
        findPreference<Preference>(DELETE_PROJECT_KEY)!!.onPreferenceClickListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            var resetDialogPreference: ResetDialogPreference? = null
            if (preference is ResetDialogPreference) {
                resetDialogPreference = preference
            }
            if (resetDialogPreference != null) {
                val dialogFragment = ResetDialogPreferenceFragmentCompat.newInstance(preference.key)
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                "odk_preferences" -> {
                    val intent = Intent(activity, GeneralPreferencesActivity::class.java)
                    intent.putExtra(GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE, true)
                    startActivity(intent)
                }
                AdminKeys.KEY_IMPORT_SETTINGS -> {
                    val pref = Intent(activity, QRCodeTabsActivity::class.java)
                    startActivity(pref)
                }
                DELETE_PROJECT_KEY -> AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.delete_project)
                    .setMessage(R.string.delete_project_confirm_message)
                    .setNegativeButton(R.string.delete_project_no) { _: DialogInterface?, _: Int -> }
                    .setPositiveButton(R.string.delete_project_yes) { _: DialogInterface?, _: Int -> deleteProject() }
                    .show()
                "main_menu" -> displayPreferences(MainMenuAccessPreferencesFragment())
                "user_settings" -> displayPreferences(UserSettingsAccessPreferencesFragment())
                "form_entry" -> displayPreferences(FormEntryAccessPreferencesFragment())
            }
            return true
        }
        return false
    }

    private fun displayPreferences(fragment: Fragment?) {
        if (fragment != null) {
            fragment.arguments = arguments
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.preferences_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun preventOtherWaysOfEditingForm() {
        val fragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.preferences_fragment_container) as FormEntryAccessPreferencesFragment
        fragment.preventOtherWaysOfEditingForm()
    }

    fun deleteProject() {
        when (val deleteProjectResult = projectDeleter.deleteCurrentProject()) {
            is UnsentInstances -> {
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.cannot_delete_project_title)
                    .setMessage(R.string.cannot_delete_project_message_one)
                    .setPositiveButton(R.string.ok, null)
                    .show()
            }
            is RunningBackgroundJobs -> {
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.cannot_delete_project_title)
                    .setMessage(R.string.cannot_delete_project_message_two)
                    .setPositiveButton(R.string.ok, null)
                    .show()
            }
            is DeletedSuccessfully -> {
                val newCurrentProject = deleteProjectResult.newCurrentProject
                if (newCurrentProject != null) {
                    ActivityUtils.startActivityAndCloseAllOthers(
                        requireActivity(),
                        MainMenuActivity::class.java
                    )
                    showLongToast(getString(R.string.switched_project, newCurrentProject.name))
                } else {
                    ActivityUtils.startActivityAndCloseAllOthers(
                        requireActivity(),
                        SplashScreenActivity::class.java
                    )
                }
            }
        }
    }

    companion object {
        const val DELETE_PROJECT_KEY = "delete_project"
    }
}
