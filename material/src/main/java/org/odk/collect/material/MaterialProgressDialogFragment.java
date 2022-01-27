package org.odk.collect.material;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.DialogInterface.BUTTON_NEGATIVE;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.Serializable;

/**
 * Provides a reusable progress dialog implemented with {@link MaterialAlertDialogBuilder}. Progress
 * dialogs don't appear in the Material guidelines/specs due to the design language's instistance
 * that progress shouldn't block the user - this is pretty unrealistic for the app in it's current
 * state so having a reliable "Material" version of the Android progress dialog is useful.
 */
public class MaterialProgressDialogFragment extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String CANCELABLE = "true";

    private View dialogView;

    /**
     * Override to have something cancelled when the ProgressDialog's cancel button is pressed
     */
    protected OnCancelCallback getOnCancelCallback() {
        return null;
    }

    /**
     * Override to show cancel button with returned text
     */
    protected String getCancelButtonText() {
        return null;
    }

    public String getTitle() {
        return getArguments().getString(TITLE);
    }

    public String getMessage() {
        return getArguments().getString(MESSAGE);
    }

    public void setTitle(String title) {
        setArgument(TITLE, title);

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
        }
    }

    public void setMessage(String message) {
        setArgument(MESSAGE, message);

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
        }
    }

    @Override
    public void setCancelable(boolean cancelable) {
        setArgument(CANCELABLE, cancelable);
        super.setCancelable(cancelable);
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
        OnCancelCallback onCancelCallback = getOnCancelCallback();
        if (onCancelCallback != null) {
            onCancelCallback.cancel();
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
                getOnCancelCallback().cancel();
            });
        }
    }

    private void setArgument(String key, Serializable value) {
        if (getArguments() == null) {
            setArguments(new Bundle());
        }

        getArguments().putSerializable(key, value);
    }

    public interface OnCancelCallback {
        boolean cancel();
    }

    public View getDialogView() {
        return dialogView;
    }
}
