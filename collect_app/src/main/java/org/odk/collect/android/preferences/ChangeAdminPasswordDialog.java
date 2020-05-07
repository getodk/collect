package org.odk.collect.android.preferences;

import androidx.fragment.app.DialogFragment;

public class ChangeAdminPasswordDialog extends DialogFragment {


    public interface ChangeAdminPasswordDialogCallback {
        void onPassWordChanged(String pw);
        void onEmptyPasswordSubmitted();
    }
}
