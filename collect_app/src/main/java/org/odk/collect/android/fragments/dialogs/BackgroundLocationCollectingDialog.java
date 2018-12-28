/*
 * Copyright 2018 Nafundi
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
import android.support.v4.app.FragmentManager;

import org.odk.collect.android.R;

import timber.log.Timber;

public class BackgroundLocationCollectingDialog extends DialogFragment {

    public static final String BACKGROUND_LOCATION_COLLECTING_DIALOG_TAG = "backgroundLocationCollectingDialogTag";

    public static BackgroundLocationCollectingDialog newInstance() {
        return new BackgroundLocationCollectingDialog();
    }

    /*
    We keep this just in case to avoid problems if someone tries to show a dialog after
    the activity’s state have been saved. Basically it shouldn't take place since we should control
    the activity state if we want to show a dialog (especially after long tasks).
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            manager
                    .beginTransaction()
                    .add(this, tag)
                    .commit();
        } catch (IllegalStateException e) {
            Timber.w(e);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_place_black)
                .setTitle(R.string.background_location_collecting_dialog_title)
                .setMessage(R.string.background_location_collecting_dialog__message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .create();
    }
}
