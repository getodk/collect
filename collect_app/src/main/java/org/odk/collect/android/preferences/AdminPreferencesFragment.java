/*
 * Copyright (C) 2017 Shobhit
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.utilities.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.AdminKeys.KEY_IMPORT_SETTINGS;


public class AdminPreferencesFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String ADMIN_PREFERENCES = "admin_prefs";

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(getString(R.string.admin_preferences));
        boolean hasHardwareMenu =
                ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(getActivity()));
        if (!hasHardwareMenu) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(ADMIN_PREFERENCES);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.admin_preferences);

        findPreference(KEY_CHANGE_ADMIN_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(KEY_IMPORT_SETTINGS).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey()) {

            case KEY_CHANGE_ADMIN_PASSWORD:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View dialogView = factory.inflate(R.layout.password_dialog_layout, null);
                final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.pwd_field);
                final CheckBox passwordCheckBox = (CheckBox) dialogView.findViewById(R.id.checkBox2);
                passwordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (!passwordCheckBox.isChecked()) {
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        } else {
                            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    }
                });
                builder.setTitle(R.string.change_admin_password);
                builder.setView(dialogView);
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pw = passwordEditText.getText().toString();
                        if (!pw.equals("")) {
                            SharedPreferences.Editor editor = getActivity()
                                    .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, pw);
                            ToastUtils.showShortToast(R.string.admin_password_changed);
                            editor.apply();
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "CHANGED");
                        } else {
                            SharedPreferences.Editor editor = getActivity()
                                    .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                            editor.putString(KEY_ADMIN_PW, "");
                            editor.apply();
                            ToastUtils.showShortToast(R.string.admin_password_disabled);
                            dialog.dismiss();
                            Collect.getInstance().getActivityLogger()
                                    .logAction(this, "AdminPasswordDialog", "DISABLED");
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Collect.getInstance().getActivityLogger().logAction(this, "AdminPasswordDialog", "CANCELLED");
                    }
                });

                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();

                break;

            case KEY_IMPORT_SETTINGS:
                getActivity().getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new ShowQRCodeFragment())
                        .addToBackStack(null)
                        .commit();
                break;
        }
        return true;
    }
}
