package org.odk.collect.android.preferences;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ServerAuthDialogBinding;
import org.odk.collect.android.injection.DaggerUtils;

import javax.inject.Inject;

public class ServerAuthDialogFragment extends DialogFragment {

    @Inject
    PreferencesDataSourceProvider preferencesDataSourceProvider;

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

        PreferencesDataSource generalPreferences = preferencesDataSourceProvider.getGeneralPreferences();
        binding.usernameEdit.setText(generalPreferences.getString(GeneralKeys.KEY_USERNAME));
        binding.passwordEdit.setText(generalPreferences.getString(GeneralKeys.KEY_PASSWORD));

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.server_requires_auth)
                .setMessage(requireContext().getString(R.string.server_auth_credentials, generalPreferences.getString(GeneralKeys.KEY_SERVER_URL)))
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    generalPreferences.save(GeneralKeys.KEY_USERNAME, binding.usernameEdit.getText().toString());
                    generalPreferences.save(GeneralKeys.KEY_PASSWORD, binding.passwordEdit.getText().toString());
                })
                .create();
    }

    public View getDialogView() {
        return dialogView;
    }
}
