package org.odk.collect.android.mainmenu

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.instancemanagement.InstanceDiskSynchronizer
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

class MainMenuViewModel(
    private val application: Application,
    private val versionInformation: VersionInformation,
    private val settingsProvider: SettingsProvider,
    private val instancesAppState: InstancesAppState,
    private val scheduler: Scheduler
) : ViewModel() {

    val version: String
        get() = versionInformation.versionToDisplay

    val versionCommitDescription: String?
        get() {
            var commitDescription = ""
            if (versionInformation.commitCount != null) {
                commitDescription =
                    appendToCommitDescription(commitDescription, versionInformation.commitCount.toString())
            }
            if (versionInformation.commitSHA != null) {
                commitDescription =
                    appendToCommitDescription(commitDescription, versionInformation.commitSHA!!)
            }
            if (versionInformation.isDirty) {
                commitDescription = appendToCommitDescription(commitDescription, "dirty")
            }
            return if (commitDescription.isNotEmpty()) {
                commitDescription
            } else {
                null
            }
        }

    fun shouldEditSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
    }

    fun shouldSendFinalizedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SEND_FINALIZED)
    }

    fun shouldViewSentFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT)
    }

    fun shouldGetBlankFormButtonBeVisible(): Boolean {
        val buttonEnabled = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_GET_BLANK)
        return !isMatchExactlyEnabled() && buttonEnabled
    }

    fun shouldDeleteSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_DELETE_SAVED)
    }

    private fun isMatchExactlyEnabled(): Boolean {
        return SettingsUtils.getFormUpdateMode(application, settingsProvider.getUnprotectedSettings()) == FormUpdateMode.MATCH_EXACTLY
    }

    private fun appendToCommitDescription(commitDescription: String, part: String): String {
        return if (commitDescription.isEmpty()) {
            part
        } else {
            "$commitDescription-$part"
        }
    }

    fun refreshInstances() {
        scheduler.immediate<Any?>({
            InstanceDiskSynchronizer(settingsProvider).doInBackground()
            instancesAppState.update()
            null
        }) { }
    }

    val editableInstancesCount: LiveData<Int>
        get() = instancesAppState.editableCount

    val sendableInstancesCount: LiveData<Int>
        get() = instancesAppState.sendableCount

    val sentInstancesCount: LiveData<Int>
        get() = instancesAppState.sentCount
}
