package org.odk.collect.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.javarosawrapper.FormController;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.PlayServicesUtil;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate {

    private final AppCompatActivity context;
    private final FormControllerProvider formControllerProvider;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;

    public FormEntryMenuDelegate(AppCompatActivity context, FormControllerProvider formControllerProvider, AnswersProvider answersProvider, FormIndexAnimationHandler formIndexAnimationHandler) {
        this.context = context;
        this.formControllerProvider = formControllerProvider;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;
    }

    public void onCreate(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    public void onPrepare(Menu menu) {
        FormController formController = formControllerProvider.getFormController();

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
                && PlayServicesUtil.isGooglePlayServicesAvailable(context)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(isInRepeat());
    }

    public boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                Intent pref = new Intent(context, PreferencesActivity.class);
                context.startActivity(pref);
                return true;

            case R.id.menu_add_repeat:
                getFormSaveViewModel().saveAnswersForScreen(answersProvider.getAnswers());
                getFormEntryViewModel().promptForNewRepeat();
                formIndexAnimationHandler.handle(getFormEntryViewModel().getCurrentIndex());
                return true;
        }

        return false;
    }

    private FormEntryViewModel getFormEntryViewModel() {
        return ViewModelProviders.of(context).get(FormEntryViewModel.class);
    }

    private FormSaveViewModel getFormSaveViewModel() {
        return ViewModelProviders.of(context).get(FormSaveViewModel.class);
    }

    private boolean isInRepeat() {
        FormController formController = formControllerProvider.getFormController();

        if (formController != null) {
            return formController.indexContainsRepeatableGroup();
        } else {
            return false;
        }
    }
}
