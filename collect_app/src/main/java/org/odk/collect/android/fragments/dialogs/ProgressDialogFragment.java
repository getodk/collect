package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import timber.log.Timber;

public class ProgressDialogFragment extends DialogFragment {

    public static final String COLLECT_PROGRESS_DIALOG_TAG = "collectProgressDialogTag";

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    private String message;

    public static ProgressDialogFragment newInstance(@Nullable String title, @Nullable String message) {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        dialogFragment.setArguments(title, message);
        return dialogFragment;
    }

    public ProgressDialogFragment setArguments(String title, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putString(MESSAGE, message);
        this.setArguments(bundle);

        return this;
    }

    /**
     * Override to have something cancelled when the ProgressDialog's cancel button is pressed
     */
    protected Cancellable getCancellable() {
        return null;
    }

    public void setMessage(String message) {
        ProgressDialog dialog = (ProgressDialog) getDialog();

        if (dialog != null) {
            dialog.setMessage(message);
        } else {
            this.message = message;
        }
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

        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        String title = getArguments().getString(TITLE);
        if (title != null) {
            dialog.setTitle(title);
        }

        if (message != null) {
            dialog.setMessage(message);
        } else {
            dialog.setMessage(getArguments().getString(MESSAGE));
        }

        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Cancellable cancellable = getCancellable();
        if (cancellable != null) {
            cancellable.cancel();
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public interface Cancellable {
        boolean cancel();
    }
}