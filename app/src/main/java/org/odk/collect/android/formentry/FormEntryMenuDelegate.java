package org.odk.collect.android.formentry;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MenuDelegate;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProtectedProjectKeys;

public class FormEntryMenuDelegate implements MenuDelegate {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormEntryViewModel formEntryViewModel;
    private final BackgroundLocationViewModel backgroundLocationViewModel;
    private final BackgroundAudioViewModel backgroundAudioViewModel;

    private final AudioRecorder audioRecorder;
    private final SettingsProvider settingsProvider;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider,
                                 FormEntryViewModel formEntryViewModel, AudioRecorder audioRecorder,
                                 BackgroundLocationViewModel backgroundLocationViewModel,
                                 BackgroundAudioViewModel backgroundAudioViewModel,
                                 SettingsProvider settingsProvider) {
        this.activity = activity;
        this.answersProvider = answersProvider;

        this.audioRecorder = audioRecorder;
        this.formEntryViewModel = formEntryViewModel;
        this.backgroundLocationViewModel = backgroundLocationViewModel;
        this.backgroundAudioViewModel = backgroundAudioViewModel;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        FormController formController = formEntryViewModel.getFormController();

        boolean useability;

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(settingsProvider.getUnprotectedSettings().getBoolean(KEY_BACKGROUND_LOCATION));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(formEntryViewModel.canAddRepeat());
        menu.findItem(R.id.menu_record_audio).setVisible(formEntryViewModel.hasBackgroundRecording().getValue());
        menu.findItem(R.id.menu_record_audio).setChecked(backgroundAudioViewModel.isBackgroundRecordingEnabled().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_repeat) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogFragmentUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formEntryViewModel.updateAnswersForScreen(answersProvider.getAnswers(), false);
                formEntryViewModel.promptForNewRepeat();
            }

            return true;
        } else if (item.getItemId() == R.id.menu_preferences) {
            if (audioRecorder.isRecording()) {
                DialogFragmentUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                Intent pref = new Intent(activity, ProjectPreferencesActivity.class);
                activity.startActivityForResult(pref, ApplicationConstants.RequestCodes.CHANGE_SETTINGS);
            }

            return true;
        } else if (item.getItemId() == R.id.track_location) {
            backgroundLocationViewModel.backgroundLocationPreferenceToggled(settingsProvider.getUnprotectedSettings());
            return true;
        } else if (item.getItemId() == R.id.menu_goto) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogFragmentUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formEntryViewModel.updateAnswersForScreen(answersProvider.getAnswers(), false);
                formEntryViewModel.openHierarchy();
                Intent i = new Intent(activity, FormHierarchyActivity.class);
                i.putExtra(FormHierarchyActivity.EXTRA_SESSION_ID, formEntryViewModel.getSessionId());
                activity.startActivityForResult(i, ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY);
            }

            return true;
        } else if (item.getItemId() == R.id.menu_record_audio) {
            boolean enabled = !item.isChecked();

            if (!enabled) {
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(R.string.stop_recording_confirmation)
                        .setPositiveButton(R.string.disable_recording, (dialog, which) -> backgroundAudioViewModel.setBackgroundRecordingEnabled(false))
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show();
            } else {
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(R.string.background_audio_recording_enabled_explanation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();

                backgroundAudioViewModel.setBackgroundRecordingEnabled(true);
            }

            return true;
        } else {
            return false;
        }
    }
}
