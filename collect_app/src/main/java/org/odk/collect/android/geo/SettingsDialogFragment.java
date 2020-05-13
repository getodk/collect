package org.odk.collect.android.geo;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;

public class SettingsDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.input_method))
                .setPositiveButton(getString(R.string.start), (dialog, id) -> {
                    dialog.cancel();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.cancel();
                    dismiss();
                })
                .create();
    }
}
