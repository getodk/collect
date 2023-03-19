/*
 * Copyright 2019 Nafundi
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
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;

import timber.log.Timber;

public class LocationProvidersDisabledDialog extends DialogFragment {

    public static final String LOCATION_PROVIDERS_DISABLED_DIALOG_TAG = "locationProvidersDisabledDialogTag";

    public static LocationProvidersDisabledDialog newInstance() {
        return new LocationProvidersDisabledDialog();
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

        return new MaterialAlertDialogBuilder(getActivity())
                .setIcon(R.drawable.ic_room_black_24dp)
                .setTitle(R.string.provider_disabled_error)
                .setMessage(R.string.location_providers_disabled_dialog_message)
                .setPositiveButton(R.string.go_to_settings, (dialog, id) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .create();
    }
}
