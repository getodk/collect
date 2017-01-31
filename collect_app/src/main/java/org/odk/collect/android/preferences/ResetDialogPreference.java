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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ResetUtility;

public class ResetDialogPreference extends DialogPreference {

    private CheckBox mPreferences;
    private CheckBox mInstances;
    private CheckBox mForms;
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public ResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.reset_dialog_layout);
        mContext = context;
    }

    @Override
    public void onBindDialogView(View view) {
        mPreferences = (CheckBox) view.findViewById(R.id.preferences);
        mInstances = (CheckBox) view.findViewById(R.id.instances);
        mForms = (CheckBox) view.findViewById(R.id.forms);
        super.onBindDialogView(view);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    private void resetSelected() {
        final boolean resetPreferences = mPreferences.isChecked();
        final boolean resetInstances = mInstances.isChecked();
        final boolean resetForms = mForms.isChecked();

        if (resetPreferences || resetInstances || resetForms) {
            showProgressDialog();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    new ResetUtility().reset(getContext(), resetPreferences, resetInstances, resetForms);
                    hideProgressDialog();
                }
            };
            new Thread(runnable).start();
        } else {
            Toast.makeText(getContext(), R.string.reset_dialog_nothing, Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(getContext(),
                mContext.getString(R.string.please_wait),
                mContext.getString(R.string.reset_in_progress),
                true);
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
        ((AdminPreferencesActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.resetting_finished, Toast.LENGTH_LONG).show();
            }
        });
    }
}