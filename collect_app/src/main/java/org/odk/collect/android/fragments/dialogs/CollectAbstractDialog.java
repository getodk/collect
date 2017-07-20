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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public abstract class CollectAbstractDialog extends DialogFragment {

    public static final String COLLECT_DIALOG_TAG = "collectDialogTag";
    protected static final String DIALOG_TITLE = "dialogTitle";
    protected static final String ICON_ID = "iconId";
    protected static final String MESSAGE = "message";
    protected static final String BUTTON_TITLE = "buttonTitle";

    protected AlertDialog.Builder builder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments().getString(DIALOG_TITLE) != null) {
            builder.setTitle(getArguments().getString(DIALOG_TITLE));
        }

        if (getArguments().getString(MESSAGE) != null) {
            builder.setMessage(getArguments().getString(MESSAGE));
        }

        if (getArguments().getInt(ICON_ID) != 0) {
            builder.setIcon(getArguments().getInt(ICON_ID));
        }

        setCancelable(false);

        return builder.create();
    }
}