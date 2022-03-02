package org.odk.collect.android.formmanagement

import android.app.Activity
import android.content.Intent
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE
import org.odk.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_EDIT_SAVED

class FormNavigator(
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsProvider: SettingsProvider,
    private val instancesRepositoryProvider: () -> InstancesRepository
) {

    fun editInstance(activity: Activity, instanceId: Long) {
        val projectId = currentProjectProvider.getCurrentProject().uuid
        val uri = InstancesContract.getUri(projectId, instanceId)
        activity.startActivity(
            Intent(activity, FormEntryActivity::class.java).also {
                it.action = Intent.ACTION_EDIT
                it.data = uri

                val editingDisabled =
                    !settingsProvider.getProtectedSettings().getBoolean(KEY_EDIT_SAVED)
                val status = instancesRepositoryProvider().get(instanceId)?.status

                if (editingDisabled ||
                    status == Instance.STATUS_SUBMITTED ||
                    status == Instance.STATUS_SUBMISSION_FAILED
                ) {
                    it.putExtra(FORM_MODE, VIEW_SENT)
                }
            }
        )
    }

    fun newInstance(activity: Activity, formId: Long) {
        val projectId = currentProjectProvider.getCurrentProject().uuid
        activity.startActivity(
            Intent(activity, FormEntryActivity::class.java).also {
                it.action = Intent.ACTION_EDIT
                it.data = FormsContract.getUri(projectId, formId)
            }
        )
    }
}
