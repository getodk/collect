package org.odk.collect.android.preferences.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ServerAuthDialogBinding;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;

import javax.inject.Inject;

public class ServerAuthDialogFragment extends DialogFragment {

    @Inject
    SettingsProvider settingsProvider;

    private View dialogView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ServerAuthDialogBinding binding = ServerAuthDialogBinding.inflate(requireActivity().getLayoutInflater());
        dialogView = binding.getRoot();

        Settings generalSettings = settingsProvider.getUnprotectedSettings();
        binding.usernameEdit.setText(generalSettings.getString(ProjectKeys.KEY_USERNAME));
        binding.passwordEdit.setText(generalSettings.getString(ProjectKeys.KEY_PASSWORD));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.server_requires_auth)
                .setMessage(requireContext().getString(R.string.server_auth_credentials, generalSettings.getString(ProjectKeys.KEY_SERVER_URL)))
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    generalSettings.save(ProjectKeys.KEY_USERNAME, binding.usernameEdit.getText().toString());
                    generalSettings.save(ProjectKeys.KEY_PASSWORD, binding.passwordEdit.getText().toString());
                })
                .create();
    }

    public View getDialogView() {
        return dialogView;
    }
}
