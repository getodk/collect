package org.odk.collect.android.preferences.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.ProjectResetter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog.RESET_SETTINGS_RESULT_DIALOG_TAG;
import static org.odk.collect.android.utilities.ProjectResetter.ResetAction.RESET_PREFERENCES;

public class ResetDialogPreferenceFragmentCompat extends PreferenceDialogFragmentCompat implements CompoundButton.OnCheckedChangeListener {

    @Inject
    ProjectResetter projectResetter;

    private ProgressDialog progressDialog;
    private AppCompatCheckBox preferences;
    private AppCompatCheckBox instances;
    private AppCompatCheckBox forms;
    private AppCompatCheckBox layers;
    private AppCompatCheckBox cache;

    private Context context;

    public static ResetDialogPreferenceFragmentCompat newInstance(String key) {
        ResetDialogPreferenceFragmentCompat fragment = new ResetDialogPreferenceFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);

        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onBindDialogView(View view) {
        preferences = view.findViewById(R.id.preferences);
        instances = view.findViewById(R.id.instances);
        forms = view.findViewById(R.id.forms);
        layers = view.findViewById(R.id.layers);
        cache = view.findViewById(R.id.cache);
        preferences.setOnCheckedChangeListener(this);
        instances.setOnCheckedChangeListener(this);
        forms.setOnCheckedChangeListener(this);
        layers.setOnCheckedChangeListener(this);
        cache.setOnCheckedChangeListener(this);
        super.onBindDialogView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        adjustResetButtonAccessibility();
    }

    @Override
    public void onDetach() {
        preferences = null;
        instances = null;
        forms = null;
        layers = null;
        cache = null;
        super.onDetach();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }

    private void resetSelected() {
        final List<Integer> resetActions = new ArrayList<>();

        if (preferences.isChecked()) {
            resetActions.add(RESET_PREFERENCES);
        }
        if (instances.isChecked()) {
            resetActions.add(ProjectResetter.ResetAction.RESET_INSTANCES);
        }
        if (forms.isChecked()) {
            resetActions.add(ProjectResetter.ResetAction.RESET_FORMS);
        }
        if (layers.isChecked()) {
            resetActions.add(ProjectResetter.ResetAction.RESET_LAYERS);
        }
        if (cache.isChecked()) {
            resetActions.add(ProjectResetter.ResetAction.RESET_CACHE);
        }

        if (!resetActions.isEmpty()) {
            new AsyncTask<Void, Void, List<Integer>>() {
                @Override
                protected void onPreExecute() {
                    showProgressDialog();
                }

                @Override
                protected List<Integer> doInBackground(Void... voids) {
                    return projectResetter.reset(resetActions);
                }

                @Override
                protected void onPostExecute(List<Integer> failedResetActions) {
                    handleResult(resetActions, failedResetActions);
                    hideProgressDialog();
                }
            }.execute();
        }
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(context,
                        context.getString(R.string.please_wait),
                        context.getString(R.string.reset_in_progress),
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
                        resultMessage.append(String.format(context.getString(R.string.reset_settings_result),
                                context.getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(context.getString(R.string.reset_settings_result),
                                context.getString(R.string.success)));
                    }
                    break;
                case ProjectResetter.ResetAction.RESET_INSTANCES:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(context.getString(R.string.reset_saved_forms_result),
                                context.getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(context.getString(R.string.reset_saved_forms_result),
                                context.getString(R.string.success)));
                    }
                    break;
                case ProjectResetter.ResetAction.RESET_FORMS:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(context.getString(R.string.reset_blank_forms_result),
                                context.getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(context.getString(R.string.reset_blank_forms_result),
                                context.getString(R.string.success)));
                    }
                    break;
                case ProjectResetter.ResetAction.RESET_CACHE:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(context.getString(R.string.reset_cache_result),
                                context.getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(context.getString(R.string.reset_cache_result),
                                context.getString(R.string.success)));
                    }
                    break;
                case ProjectResetter.ResetAction.RESET_LAYERS:
                    if (failedResetActions.contains(action)) {
                        resultMessage.append(String.format(context.getString(R.string.reset_layers_result),
                                context.getString(R.string.error_occured)));
                    } else {
                        resultMessage.append(String.format(context.getString(R.string.reset_layers_result),
                                context.getString(R.string.success)));
                    }
                    break;
            }
            if (resetActions.indexOf(action) < resetActions.size() - 1) {
                resultMessage.append("\n\n");
            }
        }
        if (!((CollectAbstractActivity) context).isInstanceStateSaved()) {
            ((CollectAbstractActivity) context).runOnUiThread(() -> {
                if (resetActions.contains(RESET_PREFERENCES)) {
                    ((CollectAbstractActivity) context).recreate();
                }
                ResetSettingsResultDialog resetSettingsResultDialog = ResetSettingsResultDialog.newInstance(String.valueOf(resultMessage));
                try {
                    resetSettingsResultDialog.show(((CollectAbstractActivity) context).getSupportFragmentManager(), RESET_SETTINGS_RESULT_DIALOG_TAG);
                } catch (ClassCastException e) {
                    Timber.i(e);
                }
            });
        }
        context = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        adjustResetButtonAccessibility();
    }

    public void adjustResetButtonAccessibility() {
        if (preferences.isChecked() || instances.isChecked() || forms.isChecked()
                || layers.isChecked() || cache.isChecked()) {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        } else {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getPartiallyTransparentColor(((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor()));
        }
    }

    private int getPartiallyTransparentColor(int color) {
        return Color.argb(150, Color.red(color), Color.green(color), Color.blue(color));
    }
}