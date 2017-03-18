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
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.javarosa.core.model.FormDef;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;



/**
 * Handles admin preferences, which are password-protectable and govern which app features and
 * general preferences the end user of the app will be able to see.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 *         option)
 */
public class AdminPreferencesActivity extends PreferenceActivity {
    private static final int SAVE_PREFS_MENU = Menu.FIRST;
<<<<<<< HEAD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.admin_preferences));

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(ADMIN_PREFERENCES);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.admin_preferences);

<<<<<<< HEAD
=======

>>>>>>> d118f43170d3d8fa17e074727a20898b2444fc25
        Preference mChangeAdminPwPreference = pref(KEY_CHANGE_ADMIN_PASSWORD);
        mChangeAdminPwPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder b = new AlertDialog.Builder(AdminPreferencesActivity.this);

                LayoutInflater factory = LayoutInflater.from(AdminPreferencesActivity.this);
                final View dialogView = factory.inflate(R.layout.password_dialog_layout, null);

                final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.pwd_field);
                final EditText verifyEditText = (EditText) dialogView.findViewById(R.id.verify_field);

                b.setTitle(R.string.change_admin_password);
                b.setView(dialogView);
                b.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pw = passwordEditText.getText().toString();
                        String ver = verifyEditText.getText().toString();

                        if (!pw.equalsIgnoreCase("") && !ver.equalsIgnoreCase("") && pw.equals(ver)) {
                            // passwords are the same
                            SharedPreferences.Editor editor = getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, pw);
                            Toast.makeText(AdminPreferencesActivity.this,
                                    R.string.admin_password_changed, Toast.LENGTH_SHORT).show();
                            editor.commit();
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "CHANGED");
                        } else if (pw.equalsIgnoreCase("") && ver.equalsIgnoreCase("")) {
                            SharedPreferences.Editor editor = getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, "");
                            editor.commit();
                            Toast.makeText(AdminPreferencesActivity.this,
                                    R.string.admin_password_disabled, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "DISABLED");
                        } else {
                            Toast.makeText(AdminPreferencesActivity.this,
                                    R.string.admin_password_mismatch, Toast.LENGTH_SHORT).show();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "MISMATCH");
                        }
                    }
                });
                b.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Collect.getInstance().getActivityLogger().logAction(this, "AdminPasswordDialog", "CANCELLED");
                    }
                });

                b.setCancelable(false);
                AlertDialog dialog = b.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, SAVE_PREFS_MENU, 0, R.string.save_preferences)
                .setIcon(R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SAVE_PREFS_MENU:
                File writeDir = new File(Collect.SETTINGS);
                if (!writeDir.exists()) {
                    if (!writeDir.mkdirs()) {
                        Toast.makeText(
                                this,
                                "Error creating directory "
                                        + writeDir.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                File dst = new File(writeDir.getAbsolutePath()
                        + "/collect.settings");
                boolean success = AdminPreferencesActivity.saveSharedPreferencesToFile(dst, this);
                if (success) {
                    Toast.makeText(
                            this,
                            "Settings successfully written to "
                                    + dst.getAbsolutePath(), Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(this,
                            "Error writing settings to " + dst.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
=======
    public static String ADMIN_PREFERENCES = "admin_prefs";
>>>>>>> upstream/master

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
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new AdminPreferencesFragment()).commit();
        setTitle(getString(R.string.admin_preferences));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, SAVE_PREFS_MENU, 0, R.string.save_preferences)
                .setIcon(R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SAVE_PREFS_MENU:
                File writeDir = new File(Collect.SETTINGS);
                if (!writeDir.exists()) {
                    if (!writeDir.mkdirs()) {
                        ToastUtils.showShortToast("Error creating directory "
                                        + writeDir.getAbsolutePath());
                        return false;
                    }
                }

                File dst = new File(writeDir.getAbsolutePath()
                        + "/collect.settings");
                boolean success = AdminPreferencesActivity.saveSharedPreferencesToFile(dst, this);
                if (success) {
                    ToastUtils.showLongToast("Settings successfully written to "
                            + dst.getAbsolutePath());
                } else {
                    ToastUtils.showLongToast("Error writing settings to " + dst.getAbsolutePath());
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
>>>>>>> upstream/master
>>>>>>> d118f43170d3d8fa17e074727a20898b2444fc25
}
