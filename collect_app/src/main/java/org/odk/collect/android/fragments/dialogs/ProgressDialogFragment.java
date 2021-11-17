package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.odk.collect.android.R;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProgressDialogFragment extends DialogFragment {

    public static final String COLLECT_PROGRESS_DIALOG_TAG = "collectProgressDialogTag";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String CANCELABLE = "true";

    private View dialogView;

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

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
        }
    }

    public void setMessage(String message) {
        setArgument(message, MESSAGE);

        AlertDialog dialog = (AlertDialog) getDialog();
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

        dialogView = requireActivity().getLayoutInflater().inflate(R.layout.progress_dialog, null, false);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

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

    private void setupView(@NonNull AlertDialog dialog) {
        if (getArguments() != null && getArguments().getString(TITLE) != null) {
            dialog.setTitle(getArguments().getString(TITLE));
        }

        if (getArguments() != null && getArguments().getString(MESSAGE) != null) {
            ((TextView) dialogView.findViewById(R.id.message)).setText(getArguments().getString(MESSAGE));
        }

        if (getArguments() != null) {
            setCancelable(getArguments().getBoolean(CANCELABLE));
        }

        if (getCancelButtonText() != null) {
            dialog.setButton(BUTTON_NEGATIVE, getCancelButtonText(), (dialog1, which) -> {
                dismiss();
                getCancellable().cancel();
            });
        }
    }

    private void setArgument(String key, String value) {
        if (getArguments() == null) {
            setArguments(new Bundle());
        }

        getArguments().putString(value, key);
    }

    public interface Cancellable {
        boolean cancel();
    }

    public View getDialogView() {
        return dialogView;
    }
}
