package org.odk.collect.android.preferences;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import android.content.DialogInterface;
import android.content.Intent;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.screens.UserInterfacePreferencesFragment;

public class SplashClickListener implements Preference.OnPreferenceClickListener {
    private final UserInterfacePreferencesFragment preferencesFragment;
    private final Preference splashPathPreference;

    public SplashClickListener(UserInterfacePreferencesFragment preferenceFragment, Preference splashPathPreference) {
        this.preferencesFragment = preferenceFragment;
        this.splashPathPreference = splashPathPreference;
    }

    private void launchImageChooser() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        preferencesFragment.startActivityForResult(i, UserInterfacePreferencesFragment.IMAGE_CHOOSER);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // if you have a value, you can clear it or select new.
        CharSequence cs = splashPathPreference.getSummary();
        if (cs != null && cs.toString().contains("/")) {

            final CharSequence[] items = {preferencesFragment.getString(R.string.select_another_image),
                    preferencesFragment.getString(R.string.use_odk_default)};

            AlertDialog.Builder builder = new AlertDialog.Builder(preferencesFragment.getActivity());
            builder.setTitle(preferencesFragment.getString(R.string.change_splash_path));
            builder.setNeutralButton(preferencesFragment.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(preferencesFragment.getString(R.string.select_another_image))) {
                        launchImageChooser();
                    } else {
                        preferencesFragment.setSplashPath(preferencesFragment.getString(R.string.default_splash_path));
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        } else {
            launchImageChooser();
        }

        return true;
    }
}
