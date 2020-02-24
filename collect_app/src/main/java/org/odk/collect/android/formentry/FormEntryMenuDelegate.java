package org.odk.collect.android.formentry;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.PlayServicesUtil;

import static org.odk.collect.android.analytics.AnalyticsEvents.LAUNCH_FORM_WITH_BG_LOCATION;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate {

    private final Context context;
    private final FormControllerProvider formControllerProvider;

    public FormEntryMenuDelegate(Context context, FormControllerProvider formControllerProvider) {
        this.context = context;
        this.formControllerProvider = formControllerProvider;
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

    private boolean isInRepeat() {
        FormController formController = formControllerProvider.getFormController();

        if (formController != null) {
            return formController.indexContainsRepeatableGroup();
        } else {
            return false;
        }
    }
}
