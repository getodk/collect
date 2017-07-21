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

import org.odk.collect.android.bundle.CollectDialogBundle;

public abstract class CollectAbstractDialog extends DialogFragment {

    protected static final String COLLECT_DIALOG_BUNDLE = "collectDialogBundle";

    protected AlertDialog.Builder builder;

    protected CollectDialogBundle collectDialogBundle;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (collectDialogBundle != null) {
            if (collectDialogBundle.getDialogTitle() != null) {
                builder.setTitle(collectDialogBundle.getDialogTitle());
            }

            if (collectDialogBundle.getDialogMessage() != null) {
                builder.setMessage(collectDialogBundle.getDialogMessage());
            }

            if (collectDialogBundle.getIcon() != null) {
                builder.setIcon(collectDialogBundle.getIcon());
            }

            setCancelable(false);
        }

        return builder.create();
    }
}