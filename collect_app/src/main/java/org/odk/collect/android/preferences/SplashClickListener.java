package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.odk.collect.android.R;

class SplashClickListener implements Preference.OnPreferenceClickListener {
    private PreferencesActivity pa;
    private PreferenceScreen splashPathPreference;

    SplashClickListener(PreferencesActivity pa, PreferenceScreen splashPathPreference) {
        this.pa = pa;
        this.splashPathPreference = splashPathPreference;
    }

    private void launchImageChooser() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        pa.startActivityForResult(i, PreferencesActivity.IMAGE_CHOOSER);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // if you have adminKey value, you can clear it or select new.
        CharSequence cs = splashPathPreference.getSummary();
        if (cs != null && cs.toString().contains("/")) {

            final CharSequence[] items = {pa.getString(R.string.select_another_image),
                    pa.getString(R.string.use_odk_default)};

            AlertDialog.Builder builder = new AlertDialog.Builder(pa);
            builder.setTitle(pa.getString(R.string.change_splash_path));
            builder.setNeutralButton(pa.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(pa.getString(R.string.select_another_image))) {
                        launchImageChooser();
                    } else {
                        pa.setSplashPath(pa.getString(R.string.default_splash_path));
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
