package org.odk.collect.android.preferences;

import androidx.fragment.app.DialogFragment;

public class ChangeAdminPasswordDialog extends DialogFragment {

    public interface ChangePasswordDialogCallback {
        void onPasswordChanged(String password);
    }
}