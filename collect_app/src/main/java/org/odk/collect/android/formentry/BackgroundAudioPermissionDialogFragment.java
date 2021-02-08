package org.odk.collect.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;

import javax.inject.Inject;

public class BackgroundAudioPermissionDialogFragment extends DialogFragment {

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    FormEntryViewModel.Factory formEntryViewModelFactory;
    FormEntryViewModel formEntryViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        formEntryViewModel = new ViewModelProvider(requireActivity(), formEntryViewModelFactory).get(FormEntryViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);

        final FragmentActivity activity = requireActivity();
        return new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.background_audio_permission_explanation)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    formEntryViewModel.errorDisplayed();
                    permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
                        @Override
                        public void granted() {
                            formEntryViewModel.startBackgroundRecording();
                        }

                        @Override
                        public void denied() {
                            activity.finish();
                        }
                    });
                })
                .create();
    }
}
