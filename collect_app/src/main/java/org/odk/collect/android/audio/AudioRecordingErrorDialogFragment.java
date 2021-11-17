package org.odk.collect.android.audio;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.MicInUseException;
import org.odk.collect.androidshared.data.Consumable;

import javax.inject.Inject;

public class AudioRecordingErrorDialogFragment extends DialogFragment {

    @Inject
    AudioRecorder audioRecorder;

    @Nullable
    Consumable<Exception> exception;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        exception = audioRecorder.failedToStart().getValue();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(R.string.ok, null);

        if (exception != null && exception.getValue() instanceof MicInUseException) {
            dialogBuilder.setMessage(R.string.mic_in_use);
        } else {
            dialogBuilder.setMessage(R.string.start_recording_failed);
        }

        return dialogBuilder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (exception != null) {
            exception.consume();
        }
    }
}
