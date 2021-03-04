package org.odk.collect.android.audio;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;

public class BackgroundAudioHelpDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.background_audio_help)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}
