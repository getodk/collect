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

/**
 * This class might be used as an universal simple dialog. You can use it if you just need to
 * display it and you don't need any callback.
 */
public class SimpleDialog extends CollectAbstractDialog {

    public static SimpleDialog newInstance(String dialogTitle, int iconId, String message, String buttonTitle) {
        SimpleDialog dialogFragment = new SimpleDialog();

        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, dialogTitle);
        bundle.putInt(ICON_ID, iconId);
        bundle.putString(MESSAGE, message);
        bundle.putString(BUTTON_TITLE, buttonTitle);

        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton(getArguments().getString(BUTTON_TITLE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        super.onCreateDialog(savedInstanceState);

        return builder.create();
    }
}
