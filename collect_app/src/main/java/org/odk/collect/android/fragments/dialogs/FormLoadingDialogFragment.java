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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.odk.collect.android.R;

import timber.log.Timber;

public class FormLoadingDialogFragment extends DialogFragment {
    public static final String FORM_LOADING_DIALOG_FRAGMENT_TAG = "formLoadingDialogFragmentTag";

    public interface FormLoadingDialogFragmentListener {
        void onCancelFormLoading();
    }

    private FormLoadingDialogFragmentListener listener;

    public static FormLoadingDialogFragment newInstance() {
        return new FormLoadingDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FormLoadingDialogFragmentListener) {
            listener = (FormLoadingDialogFragmentListener) context;
        }
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setTitle(R.string.loading_form);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setButton(getString(R.string.cancel_loading_form), (dialog1, which) -> listener.onCancelFormLoading());
        return dialog;
    }

    public void updateMessage(String message) {
        ((ProgressDialog) getDialog()).setMessage(message);
    }
}