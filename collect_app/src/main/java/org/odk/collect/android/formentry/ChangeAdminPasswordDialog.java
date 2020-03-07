package org.odk.collect.android.formentry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
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

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;

public class ChangeAdminPasswordDialog extends DialogFragment {

    private View dialogView;
    private EditText passwordEditText;
    private CheckBox passwordCheckBox;
    private static final String ADMIN_PREFERENCES = "admin_prefs";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        LayoutInflater factory = LayoutInflater.from(context);
        dialogView = factory.inflate(R.layout.password_dialog_layout, null);
        passwordEditText = dialogView.findViewById(R.id.pwd_field);
        passwordCheckBox = dialogView.findViewById(R.id.checkBox2);
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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setRetainInstance(true);

        AlertDialog alertDialog = (AlertDialog) getDialog();

        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.change_admin_password);
            builder.setView(dialogView);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pw = passwordEditText.getText().toString();
                    if (!pw.equals("")) {
                        SharedPreferences.Editor editor = getActivity()
                                .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString(KEY_ADMIN_PW, pw);
                        ToastUtils.showShortToast(R.string.admin_password_changed);
                        editor.apply();
                        dialog.dismiss();
                    } else {
                        SharedPreferences.Editor editor = getActivity()
                                .getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString(KEY_ADMIN_PW, "");
                        editor.apply();
                        ToastUtils.showShortToast(R.string.admin_password_disabled);
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
            dialog.dismiss();
        }
        super.onDestroyView();
    }
}
