package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;

import org.odk.collect.android.R;

import timber.log.Timber;

public class ProgressDialogFragment extends DialogFragment {
    public static final String COLLECT_PROGRESS_DIALOG_TAG = "collectProgressDialogTag";

    private static final String MESSAGE = "message";

    public static ProgressDialogFragment newInstance(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);

        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    /*
    We keep this just in case to avoid problems if someone tries to show a dialog after
    the activity’s state have been saved. Basically it shouldn't take place since we should control
    the activity state if we want to show a dialog (especially after long tasks).
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            manager
                    .beginTransaction()
                    .add(this, tag)
                    .commit();
        } catch (IllegalStateException e) {
            Timber.w(e);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        setRetainInstance(true);

        setCancelable(false);

        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}