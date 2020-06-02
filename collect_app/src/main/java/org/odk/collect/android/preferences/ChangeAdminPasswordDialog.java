package org.odk.collect.android.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.AdminPreferencesActivity.ADMIN_PREFERENCES;

public class ChangeAdminPasswordDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater factory = LayoutInflater.from(getActivity());
        View dialogView = factory.inflate(R.layout.password_dialog_layout, null);
        EditText passwordEditText = dialogView.findViewById(R.id.pwd_field);
        CheckBox passwordCheckBox = dialogView.findViewById(R.id.checkBox2);
        passwordEditText.requestFocus();

        passwordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!passwordCheckBox.isChecked()) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.change_admin_password);
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordEditText.getText().toString();
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(KEY_ADMIN_PW, password);

                if (password.equals("")) {
                    ToastUtils.showShortToast(R.string.admin_password_disabled);
                } else {
                    ToastUtils.showShortToast(R.string.admin_password_changed);
                }
                editor.apply();
                dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return alertDialog;
    }
}
