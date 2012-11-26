
package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

public class PasswordDialogPreference extends DialogPreference implements
        OnClickListener {

    private EditText passwordEditText;
    private EditText verifyEditText;

    public PasswordDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.password_dialog_layout);
    }

    @Override
    public void onBindDialogView(View view) {
        passwordEditText = (EditText) view.findViewById(R.id.pwd_field);
        verifyEditText = (EditText) view.findViewById(R.id.verify_field);

        final String adminPW = getPersistedString("");
        // populate the fields if a pw exists
        if (!adminPW.equalsIgnoreCase("")) {
            passwordEditText.setText(adminPW);
            passwordEditText.setSelection(passwordEditText.getText().length());
            verifyEditText.setText(adminPW);
        }

        Button positiveButton = (Button) view
                .findViewById(R.id.positive_button);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pw = passwordEditText.getText().toString();
                String ver = verifyEditText.getText().toString();

                if (!pw.equalsIgnoreCase("") && !ver.equalsIgnoreCase("") && pw.equals(ver)) {
                    // passwords are the same
                    persistString(pw);
                    Toast.makeText(PasswordDialogPreference.this.getContext(),
                            R.string.admin_password_changed, Toast.LENGTH_SHORT).show();
                    PasswordDialogPreference.this.getDialog().dismiss();
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "AdminPasswordDialog", "CHANGED");
                } else if (pw.equalsIgnoreCase("") && ver.equalsIgnoreCase("")) {
                    persistString("");
                    Toast.makeText(PasswordDialogPreference.this.getContext(),
                            R.string.admin_password_disabled, Toast.LENGTH_SHORT).show();
                    PasswordDialogPreference.this.getDialog().dismiss();
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "AdminPasswordDialog", "DISABLED");
                } else {
                    Toast.makeText(PasswordDialogPreference.this.getContext(),
                            R.string.admin_password_mismatch, Toast.LENGTH_SHORT).show();
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "AdminPasswordDialog", "MISMATCH");
                }
            }
        });

        Button negativeButton = (Button) view.findViewById(R.id.negative_button);
        negativeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PasswordDialogPreference.this.getDialog().dismiss();
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "AdminPasswordDialog", "CANCELLED");
            }

        });

        super.onBindDialogView(view);
    }

    @Override
    protected void onClick() {
        super.onClick();
        // this seems to work to pop the keyboard when the dialog appears
        // i hope this isn't a race condition
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // we get rid of the default buttons (that close the dialog every time)
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
        super.onPrepareDialogBuilder(builder);
    }

}
