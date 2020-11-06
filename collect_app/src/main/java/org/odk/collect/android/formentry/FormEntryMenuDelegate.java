package org.odk.collect.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MenuDelegate;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate implements MenuDelegate, RequiresFormController {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;
    private final FormEntryViewModel formEntryViewModel;
    private final FormSaveViewModel formSaveViewModel;
    private final BackgroundLocationViewModel backgroundLocationViewModel;

    @Nullable
    private FormController formController;
    private final AudioRecorderViewModel audioRecorderViewModel;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider, FormIndexAnimationHandler formIndexAnimationHandler) {
        this.activity = activity;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;

        audioRecorderViewModel = new ViewModelProvider(activity).get(AudioRecorderViewModel.class);
        formEntryViewModel = new ViewModelProvider(activity).get(FormEntryViewModel.class);
        formSaveViewModel = new ViewModelProvider(activity).get(FormSaveViewModel.class);
        backgroundLocationViewModel = new ViewModelProvider(activity).get(BackgroundLocationViewModel.class);
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean useability;

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(formEntryViewModel.canAddRepeat());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_repeat:
                formSaveViewModel.saveAnswersForScreen(answersProvider.getAnswers());
                formEntryViewModel.promptForNewRepeat();
                formIndexAnimationHandler.handle(formEntryViewModel.getCurrentIndex());
                return true;

            case R.id.menu_preferences:
                if (audioRecorderViewModel.isRecording().getValue()) {
                    DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
                    return true;
                }

                Intent pref = new Intent(activity, PreferencesActivity.class);
                activity.startActivity(pref);
                return true;

            case R.id.track_location:
                backgroundLocationViewModel.backgroundLocationPreferenceToggled();
                return true;
        }

        return false;
    }
}
