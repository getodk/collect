/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog;
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog;
import org.odk.collect.android.utilities.ThemeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import timber.log.Timber;

/**
 * Handles admin preferences, which are password-protectable and govern which app features and
 * general preferences the end user of the app will be able to see.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 *         option)
 */
public class AdminPreferencesActivity extends CollectAbstractActivity implements MovingBackwardsDialog.MovingBackwardsDialogListener, ResetSettingsResultDialog.ResetSettingsResultDialogListener {
    public static final String ADMIN_PREFERENCES = "admin_prefs";
    public static final String TAG = "AdminPreferencesFragment";

    public static boolean saveSharedPreferencesToFile(File dst, Context context) {
        // this should be in a thread if it gets big, but for now it's tiny
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(context);
            SharedPreferences adminPreferences = context.getSharedPreferences(
                    AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

            output.writeObject(pref.getAll());
            output.writeObject(adminPreferences.getAll());

            res = true;
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Unable to close output stream due to : %s ", ex.getMessage());
            }
        }
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(new ThemeUtils(this).getSettingsTheme());

        setTitle(R.string.admin_preferences);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new AdminPreferencesFragment(), TAG)
                    .commit();
        }
    }

    @Override
    public void preventOtherWaysOfEditingForm() {
        AdminPreferencesFragment fragment = (AdminPreferencesFragment) getFragmentManager().findFragmentByTag(TAG);
        fragment.preventOtherWaysOfEditingForm();
    }

    @Override
    public void onDialogClosed() {
        MainMenuActivity.startActivityAndCloseAllOthers(this);
    }
}
