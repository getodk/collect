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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

/**
 * This class might be used as an universal simple dialog. You can use it if you just need to
 * display it and you don't need any callback.
 */
public class SimpleDialog extends DialogFragment {

    public static final String COLLECT_DIALOG_TAG = "collectDialogTag";

    private static final String DIALOG_TITLE = "dialogTitle";
    private static final String ICON_ID = "iconId";
    private static final String MESSAGE = "message";
    private static final String BUTTON_TITLE = "buttonTitle";

    public static SimpleDialog newInstance(String dialogTitle, int iconId, String message, String buttonTitle) {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, dialogTitle);
        bundle.putInt(ICON_ID, iconId);
        bundle.putString(MESSAGE, message);
        bundle.putString(BUTTON_TITLE, buttonTitle);

        SimpleDialog dialogFragment = new SimpleDialog();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setIcon(getArguments().getInt(ICON_ID))
                .setMessage(getArguments().getString(MESSAGE))
                .setPositiveButton(getArguments().getString(BUTTON_TITLE), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }
}