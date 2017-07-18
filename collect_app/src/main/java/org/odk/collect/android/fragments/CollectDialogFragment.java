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
import android.support.v4.app.DialogFragment;

import org.odk.collect.android.bundle.CollectDialogBundle;

public class CollectDialogFragment extends DialogFragment {

    public static final String COLLECT_DIALOG_BUNDLE = "collectDialogBundle";

    public interface DialogButtonCallbacks {
        void onNegativeButtonClick(DialogInterface dialog, int actionTag);

        void onPositiveButtonClick(DialogInterface dialog, int actionTag);

        void onNeutralButtonClick(DialogInterface dialog, int actionTag);
    }

    private DialogButtonCallbacks callback;

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
            final int actionTag = collectDialogBundle.getActionTag();

            builder
                    .setTitle(collectDialogBundle.getDialogTitle())
                    .setMessage(collectDialogBundle.getDialogMessage())
                    .setCancelable(collectDialogBundle.isCancelable());

            if (collectDialogBundle.getNegativeButtonText() != null) {
                builder.setNegativeButton(collectDialogBundle.getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.onNegativeButtonClick(dialog, actionTag);
                        }
                    }
                });
            }

            if (collectDialogBundle.getPositiveButtonText() != null) {
                builder.setPositiveButton(collectDialogBundle.getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.onPositiveButtonClick(dialog, actionTag);
                        }
                    }
                });
            }

            if (collectDialogBundle.getNeutralButtonText() != null) {
                builder.setNeutralButton(collectDialogBundle.getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.onNeutralButtonClick(dialog, actionTag);
                        }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (DialogButtonCallbacks) context;
    }
}