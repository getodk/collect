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

public class MovingBackwardsDialog extends DialogFragment {

    public static final String MOVING_BACKWARDS_DIALOG_TAG = "movingBackwardsDialogTag";

    public interface MovingBackwardsDialogListener {
        void preventOtherWaysOfEditingForm();
    }

    private MovingBackwardsDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MovingBackwardsDialogListener) {
            listener = (MovingBackwardsDialogListener) context;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.moving_backwards_disabled_title)
                .setMessage(R.string.moving_backwards_disabled_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.preventOtherWaysOfEditingForm();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }
}
