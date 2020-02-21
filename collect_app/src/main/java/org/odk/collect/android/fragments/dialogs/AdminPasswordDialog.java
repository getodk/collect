package org.odk.collect.android.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.AdminPasswordProvider;

public class AdminPasswordDialog extends DialogFragment {

    public interface AdminPasswordDialogCallback {
        void onCorrectAdminPassword(Action action);
        void onIncorrectAdminPassword();
    }

    public enum Action { ADMIN_SETTINGS, STORAGE_MIGRATION }

    private AdminPasswordDialogCallback callback;

    private final Action action;

    private final AdminPasswordProvider adminPasswordProvider;

    public static AdminPasswordDialog create(AdminPasswordProvider adminPasswordProvider, Action action) {
        return new AdminPasswordDialog(adminPasswordProvider, action);
    }

    private AdminPasswordDialog(AdminPasswordProvider adminPasswordProvider, Action action) {
        this.adminPasswordProvider = adminPasswordProvider;
        this.action = action;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof AdminPasswordDialogCallback) {
            callback = (AdminPasswordDialogCallback) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.admin_password_dialog_layout, null);
        CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
        EditText input = dialogView.findViewById(R.id.editText);

        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!checkBox.isChecked()) {
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(getString(R.string.enter_admin_password))
                .setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                            if (adminPasswordProvider.getAdminPassword().equals(input.getText().toString())) {
                                callback.onCorrectAdminPassword(action);
                            } else {
                                callback.onIncorrectAdminPassword();
                            }
                            dismiss();
                        })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dismiss())
                .create();
    }
}
