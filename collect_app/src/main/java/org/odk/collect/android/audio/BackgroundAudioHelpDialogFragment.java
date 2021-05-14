package org.odk.collect.android.audio;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;

public class BackgroundAudioHelpDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.background_audio_help_fragment_layout, null);
        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}
