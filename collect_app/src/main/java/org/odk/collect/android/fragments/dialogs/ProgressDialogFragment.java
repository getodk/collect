package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;

public class ProgressDialogFragment extends DialogFragment {

    public static final String COLLECT_PROGRESS_DIALOG_TAG = "collectProgressDialogTag";

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    /**
     * Override to have something cancelled when the ProgressDialog's cancel button is pressed
     */
    protected Cancellable getCancellable() {
        return null;
    }

    /**
     * Override to show cancel button with returned text
     */
    protected String getCancelButtonText() {
        return null;
    }

    public void setTitle(String title) {
        setArgument(title, TITLE);

        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
        }
    }

    public void setMessage(String message) {
        setArgument(message, MESSAGE);

        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
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

        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setupView(dialog);
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

    private void setupView(@NonNull ProgressDialog dialog) {
        if (getArguments() != null && getArguments().getString(TITLE) != null) {
            dialog.setTitle(getArguments().getString(TITLE));
        }

        if (getArguments() != null && getArguments().getString(MESSAGE) != null) {
            dialog.setMessage(getArguments().getString(MESSAGE));
        }

        if (getCancelButtonText() != null) {
            dialog.setButton(BUTTON_NEGATIVE, getCancelButtonText(), (dialog1, which) -> {
                dismiss();
                getCancellable().cancel();
            });
        }
    }

    private void setArgument(String title, String title2) {
        if (getArguments() == null) {
            setArguments(new Bundle());
        }

        getArguments().putString(title2, title);
    }

    public interface Cancellable {
        boolean cancel();
    }
}