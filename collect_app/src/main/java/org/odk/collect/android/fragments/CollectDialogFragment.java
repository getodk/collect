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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.odk.collect.android.bundle.CollectDialogBundle;

public class CollectDialogFragment extends DialogFragment {

    public static final String COLLECT_DIALOG_BUNDLE = "collectDialogBundle";

    public static CollectDialogFragment newInstance(CollectDialogBundle collectDialogBundle) {
        CollectDialogFragment dialogFragment = new CollectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(COLLECT_DIALOG_BUNDLE, collectDialogBundle);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CollectDialogBundle collectDialogBundle = (CollectDialogBundle) getArguments().getSerializable(COLLECT_DIALOG_BUNDLE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (collectDialogBundle != null) {
            builder
                    .setTitle(collectDialogBundle.getDialogTitle())
                    .setMessage(collectDialogBundle.getDialogMessage())
                    .setCancelable(collectDialogBundle.isCancelable());

            if (collectDialogBundle.getOnNegativeButtonClickCallback() != null) {
                builder.setNegativeButton(collectDialogBundle.getNegativeButtonText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        collectDialogBundle.onNegativeButtonClickCallback.onClick(getDialog());
                    }
                });
            }

            if (collectDialogBundle.getOnPositiveButtonClickCallback() != null) {
                builder.setPositiveButton(collectDialogBundle.getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        collectDialogBundle.onPositiveButtonClickCallback.onClick(getDialog());
                    }
                });
            }

            if (collectDialogBundle.getOnNeutralButtonClickCallback() != null) {
                builder.setNeutralButton(collectDialogBundle.getNeutralButtonText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        collectDialogBundle.onNeutralButtonClickCallback.onClick(getDialog());
                    }
                });
            }

            if (collectDialogBundle.getIcon() != null) {
                builder.setIcon(collectDialogBundle.getIcon());
            }

            setCancelable(collectDialogBundle.isCancelable());
        }

        return builder.create();
    }
}