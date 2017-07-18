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

package org.odk.collect.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import org.odk.collect.android.bundle.CollectDialogBundle;

public class ResetSettingsFinalDialog extends CollectDialogFragment {

    public interface ResetSettingsFinalDialogCallbacks {
        void finishResetSettings(DialogInterface dialog);
    }

    private ResetSettingsFinalDialogCallbacks callback;

    public static ResetSettingsFinalDialog newInstance(CollectDialogBundle collectDialogBundle) {
        ResetSettingsFinalDialog dialogFragment = new ResetSettingsFinalDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(COLLECT_DIALOG_BUNDLE, collectDialogBundle);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        collectDialogBundle = (CollectDialogBundle) getArguments().getSerializable(COLLECT_DIALOG_BUNDLE);
        builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton(collectDialogBundle.getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (callback != null) {
                    callback.finishResetSettings(dialog);
                }
            }
        });

        super.onCreateDialog(savedInstanceState);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ResetSettingsFinalDialogCallbacks) context;
    }
}
