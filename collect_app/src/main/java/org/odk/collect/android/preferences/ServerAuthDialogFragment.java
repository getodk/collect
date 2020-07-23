package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ServerAuthDialogBinding;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import javax.inject.Inject;

public class ServerAuthDialogFragment extends DialogFragment {

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ServerAuthDialogBinding binding = ServerAuthDialogBinding.inflate(requireActivity().getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.server_requires_auth)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> webCredentialsUtils.saveCredentials(
                        webCredentialsUtils.getServerUrlFromPreferences(),
                        binding.usernameEdit.getText().toString(),
                        binding.passwordEdit.getText().toString()
                ))
                .create();
    }
}
