package org.odk.collect.android.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
    PreferencesProvider preferencesProvider;

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

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.server_requires_auth)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    SharedPreferences generalSharedPreferences = preferencesProvider.getGeneralSharedPreferences();
                    generalSharedPreferences.edit()
                            .putString(GeneralKeys.KEY_USERNAME, binding.usernameEdit.getText().toString())
                            .putString(GeneralKeys.KEY_PASSWORD, binding.passwordEdit.getText().toString())
                            .apply();
                })
                .create();
    }

    public View getDialogView() {
        return dialogView;
    }
}
