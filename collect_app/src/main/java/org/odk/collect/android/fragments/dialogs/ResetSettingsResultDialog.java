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

package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;

public class ResetSettingsResultDialog extends DialogFragment {
    public static final String RESET_SETTINGS_RESULT_DIALOG_TAG = "resetSettingsResultDialogTag";

    private static final String MESSAGE = "message";

    public interface ResetSettingsResultDialogListener {
        void onDialogClosed();
    }

    private ResetSettingsResultDialogListener listener;

    public static ResetSettingsResultDialog newInstance(String dialogMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, dialogMessage);

        ResetSettingsResultDialog dialogFragment = new ResetSettingsResultDialog();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ResetSettingsResultDialogListener) {
            listener = (ResetSettingsResultDialogListener) context;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        String message = getArguments() != null ? getArguments().getString(MESSAGE) : "";

        return new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.reset_app_state_result)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogClosed();
                    }
                })
                .create();
    }
}