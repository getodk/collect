package org.odk.collect.android.formentry

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormHierarchyActivity
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel
import org.odk.collect.android.formentry.questions.AnswersProvider
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.androidshared.system.PlayServicesChecker
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.strings.localization.getLocalizedString

class FormEntryMenuProvider(
    private val activity: AppCompatActivity,
    private val answersProvider: AnswersProvider,
    private val formEntryViewModel: FormEntryViewModel,
    private val audioRecorder: AudioRecorder,
    private val backgroundLocationViewModel: BackgroundLocationViewModel,
    private val backgroundAudioViewModel: BackgroundAudioViewModel,
    private val settingsProvider: SettingsProvider,
    private val formEntryMenuClickListener: FormEntryMenuClickListener
) : MenuProvider {
    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.form_menu, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        val formController = formEntryViewModel.formController

        var useability: Boolean = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_MID)
        menu.findItem(R.id.menu_save).isVisible = useability

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_JUMP_TO)
        menu.findItem(R.id.menu_goto).isVisible = useability

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_CHANGE_LANGUAGE) && formController != null && formController.getLanguages() != null && formController.getLanguages()!!.size > 1
        menu.findItem(R.id.menu_languages).isVisible = useability

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_ACCESS_SETTINGS)
        menu.findItem(R.id.menu_preferences).isVisible = useability

        if (formController != null &&
            formController.currentFormCollectsBackgroundLocation() &&
            PlayServicesChecker().isGooglePlayServicesAvailable(activity)
        ) {
            val backgroundLocation = menu.findItem(R.id.track_location)
            backgroundLocation.isVisible = true
            val isBackgroundLocationEnabled = settingsProvider.getUnprotectedSettings().getBoolean(ProjectKeys.KEY_BACKGROUND_LOCATION)
            menu.findItem(R.id.track_location).icon = if (isBackgroundLocationEnabled) {
                ContextCompat.getDrawable(activity, org.odk.collect.icons.R.drawable.ic_baseline_location_on_24)
            } else {
                ContextCompat.getDrawable(activity, org.odk.collect.icons.R.drawable.ic_baseline_location_off_24)
            }
            menu.findItem(R.id.track_location).title = if (isBackgroundLocationEnabled) {
                activity.getLocalizedString(org.odk.collect.strings.R.string.track_location_on)
            } else {
                activity.getLocalizedString(org.odk.collect.strings.R.string.track_location_off)
            }
        }

        menu.findItem(R.id.menu_add_repeat).isVisible = formEntryViewModel.canAddRepeat()

        menu.findItem(R.id.menu_record_audio).isVisible = formEntryViewModel.hasBackgroundRecording().value
        val isRecordingAudioEnabled = backgroundAudioViewModel.isBackgroundRecordingEnabled.value
        menu.findItem(R.id.menu_record_audio).icon = if (isRecordingAudioEnabled) {
            ContextCompat.getDrawable(activity, org.odk.collect.icons.R.drawable.ic_baseline_mic_24)
        } else {
            ContextCompat.getDrawable(activity, org.odk.collect.icons.R.drawable.ic_baseline_mic_off_24)
        }
        menu.findItem(R.id.menu_record_audio).title = if (isRecordingAudioEnabled) {
            activity.getLocalizedString(org.odk.collect.strings.R.string.record_audio_on)
        } else {
            activity.getLocalizedString(org.odk.collect.strings.R.string.record_audio_off)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (!allowClick(javaClass.name)) {
            return true
        }

        return when (item.itemId) {
            R.id.menu_add_repeat -> {
                if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording) {
                    showIfNotShowing(RecordingWarningDialogFragment::class.java, activity.supportFragmentManager)
                } else {
                    formEntryViewModel.updateAnswersForScreen(answersProvider.answers, false)
                    formEntryViewModel.promptForNewRepeat()
                }
                true
            }
            R.id.menu_preferences -> {
                if (audioRecorder.isRecording()) {
                    showIfNotShowing(RecordingWarningDialogFragment::class.java, activity.supportFragmentManager)
                } else {
                    formEntryViewModel.updateAnswersForScreen(answersProvider.answers, false)
                    val pref = Intent(activity, ProjectPreferencesActivity::class.java)
                    activity.startActivityForResult(pref, ApplicationConstants.RequestCodes.CHANGE_SETTINGS)
                }
                true
            }
            R.id.track_location -> {
                backgroundLocationViewModel.backgroundLocationPreferenceToggled(settingsProvider.getUnprotectedSettings())
                true
            }
            R.id.menu_goto -> {
                if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording) {
                    showIfNotShowing(RecordingWarningDialogFragment::class.java, activity.supportFragmentManager)
                } else {
                    formEntryViewModel.updateAnswersForScreen(answersProvider.answers, false)
                    formEntryViewModel.openHierarchy()
                    val i = Intent(activity, FormHierarchyActivity::class.java)
                    i.putExtra(FormHierarchyActivity.EXTRA_SESSION_ID, formEntryViewModel.sessionId)
                    activity.startActivityForResult(i, ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY)
                }
                true
            }
            R.id.menu_record_audio -> {
                val enabled = item.title == activity.getLocalizedString(org.odk.collect.strings.R.string.record_audio_off)
                if (enabled) {
                    MaterialAlertDialogBuilder(activity)
                        .setMessage(org.odk.collect.strings.R.string.background_audio_recording_enabled_explanation)
                        .setCancelable(false)
                        .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                        .create()
                        .show()
                    backgroundAudioViewModel.setBackgroundRecordingEnabled(true)
                } else {
                    MaterialAlertDialogBuilder(activity)
                        .setMessage(org.odk.collect.strings.R.string.stop_recording_confirmation)
                        .setPositiveButton(org.odk.collect.strings.R.string.disable_recording) { _: DialogInterface?, _: Int -> backgroundAudioViewModel.setBackgroundRecordingEnabled(false) }
                        .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
                        .create()
                        .show()
                }
                true
            }
            R.id.menu_validate -> {
                formEntryViewModel.saveScreenAnswersToFormController(answersProvider.answers, false)
                formEntryViewModel.validate()
                true
            }
            R.id.menu_languages -> {
                formEntryMenuClickListener.changeLanguage()
                true
            }
            R.id.menu_save -> {
                formEntryMenuClickListener.save()
                true
            }
            else -> {
                false
            }
        }
    }

    interface FormEntryMenuClickListener {
        fun changeLanguage()
        fun save()
    }
}
