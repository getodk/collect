/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import org.odk.collect.android.R;
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog;
import org.odk.collect.android.utilities.ApplicationResetter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog.RESET_SETTINGS_RESULT_DIALOG_TAG;
import static org.odk.collect.android.utilities.ApplicationResetter.ResetAction.RESET_PREFERENCES;

public class ResetDialogPreference extends DialogPreference implements CompoundButton.OnCheckedChangeListener, DialogInterface.OnClickListener {
    private AppCompatCheckBox preferences;
    private AppCompatCheckBox instances;
    private AppCompatCheckBox forms;
    private AppCompatCheckBox layers;
    private AppCompatCheckBox cache;
    private AppCompatCheckBox osmDroid;
    private ProgressDialog progressDialog;

    public ResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.reset_dialog_layout);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        preferences = (AppCompatCheckBox) holder.findViewById(R.id.preferences);
        instances = (AppCompatCheckBox) holder.findViewById(R.id.instances);
        forms = (AppCompatCheckBox) holder.findViewById(R.id.forms);
        layers = (AppCompatCheckBox) holder.findViewById(R.id.layers);
        cache = (AppCompatCheckBox) holder.findViewById(R.id.cache);
        osmDroid = (AppCompatCheckBox) holder.findViewById(R.id.osmdroid);
        preferences.setOnCheckedChangeListener(this);
        instances.setOnCheckedChangeListener(this);
        forms.setOnCheckedChangeListener(this);
        layers.setOnCheckedChangeListener(this);
        cache.setOnCheckedChangeListener(this);
        osmDroid.setOnCheckedChangeListener(this);

        super.onBindViewHolder(holder);
    }

    /*@Override
    public void onBindDialogView(View view) {
        preferences = view.findViewById(R.id.preferences);
        instances = view.findViewById(R.id.instances);
        forms = view.findViewById(R.id.forms);
        layers = view.findViewById(R.id.layers);
        cache = view.findViewById(R.id.cache);
        osmDroid = view.findViewById(R.id.osmdroid);
        preferences.setOnCheckedChangeListener(this);
        instances.setOnCheckedChangeListener(this);
        forms.setOnCheckedChangeListener(this);
        layers.setOnCheckedChangeListener(this);
        cache.setOnCheckedChangeListener(this);
        osmDroid.setOnCheckedChangeListener(this);

        super.onBindDialogView(view);
    }*/
/*
    @Override
    public void showDialog(Bundle bundle) {
        super.showDialog(bundle);
        adjustResetButtonAccessibility();
    }*/

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    private void resetSelected() {
        final List<Integer> resetActions = new ArrayList<>();

        if (preferences.isChecked()) {
            resetActions.add(RESET_PREFERENCES);
        }
        if (instances.isChecked()) {
            resetActions.add(ApplicationResetter.ResetAction.RESET_INSTANCES);
        }
        if (forms.isChecked()) {
            resetActions.add(ApplicationResetter.ResetAction.RESET_FORMS);
        }
        if (layers.isChecked()) {
            resetActions.add(ApplicationResetter.ResetAction.RESET_LAYERS);
        }
        if (cache.isChecked()) {
            resetActions.add(ApplicationResetter.ResetAction.RESET_CACHE);
        }
        if (osmDroid.isChecked()) {
            resetActions.add(ApplicationResetter.ResetAction.RESET_OSM_DROID);
        }

        if (!resetActions.isEmpty()) {
            new AsyncTask<Void, Void, List<Integer>>() {
                @Override
                protected void onPreExecute() {
                    showProgressDialog();
                }

                @Override
                protected List<Integer> doInBackground(Void... voids) {
                    return new ApplicationResetter().reset(getContext(), resetActions);
                }

                @Override
                protected void onPostExecute(List<Integer> failedResetActions) {
                    hideProgressDialog();
                    handleResult(resetActions, failedResetActions);
                }
            }.execute();
        }
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(getContext(),
                getContext().getString(R.string.please_wait),
                getContext().getString(R.string.reset_in_progress),
                true);
    }

    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    private void handleResult(final List<Integer> resetActions, List<Integer> failedResetActions) {
        final StringBuilder resultMessage = new StringBuilder();
        for (int action : resetActions) {
            switch (action) {
                case RESET_PREFERENCES:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_settings_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_settings_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
                case ApplicationResetter.ResetAction.RESET_INSTANCES:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_saved_forms_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_saved_forms_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
                case ApplicationResetter.ResetAction.RESET_FORMS:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_blank_forms_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_blank_forms_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
                case ApplicationResetter.ResetAction.RESET_CACHE:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_cache_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_cache_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
                case ApplicationResetter.ResetAction.RESET_LAYERS:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_layers_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_layers_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
                case ApplicationResetter.ResetAction.RESET_OSM_DROID:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_osm_tiles_result),
                                getContext().getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(getContext().getString(R.string.reset_osm_tiles_result),
                                getContext().getString(R.string.success)));
                    }
                    break;
            }
            if (resetActions.indexOf(action) < resetActions.size() - 1) {
                resultMessage.append("\n\n");
            }
        }
        if (!((AdminPreferencesActivity) getContext()).isInstanceStateSaved()) {
            ((AdminPreferencesActivity) getContext()).runOnUiThread(() -> {
                if (resetActions.contains(RESET_PREFERENCES)) {
                    ((AdminPreferencesActivity) getContext()).recreate();
                }
                ResetSettingsResultDialog resetSettingsResultDialog = ResetSettingsResultDialog.newInstance(String.valueOf(resultMessage));
                try {
                    resetSettingsResultDialog.show(((AdminPreferencesActivity) getContext()).getSupportFragmentManager(), RESET_SETTINGS_RESULT_DIALOG_TAG);
                } catch (ClassCastException e) {
                    Timber.i(e);
                }
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//        adjustResetButtonAccessibility();
    }
/*

    private void adjustResetButtonAccessibility() {
        if (preferences.isChecked() || instances.isChecked() || forms.isChecked()
                || layers.isChecked() || cache.isChecked() || osmDroid.isChecked()) {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        } else {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getPartiallyTransparentColor(((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor()));
        }
    }
*/

    private int getPartiallyTransparentColor(int color) {
        return Color.argb(150, Color.red(color), Color.green(color), Color.blue(color));
    }
}