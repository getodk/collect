package org.odk.collect.material;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

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
    public static final String ICON = "icon";
    public static final String POSITIVE_BUTTON_TEXT = "positive_button_text";
    public static final String NEGATIVE_BUTTON_TEXT = "negative_button_text";

    private View dialogView;
    private DialogInterface.OnClickListener onPositiveButtonClickListener;
    private DialogInterface.OnClickListener onNegativeButtonClickListener;

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

    public @DrawableRes int getIcon() {
        return getArguments().getInt(ICON);
    }

    public void setTitle(String title) {
        setArgument(TITLE, title);
        setupView();
    }

    public void setMessage(String message) {
        setArgument(MESSAGE, message);
        setupView();
    }

    public void setIcon(@DrawableRes int iconId) {
        setArgument(ICON, iconId);
        setupView();
    }

    public void setPositiveButton(String text, final DialogInterface.OnClickListener listener) {
        setArgument(POSITIVE_BUTTON_TEXT, text);
        onPositiveButtonClickListener = listener;
        setupView();
    }

    public void setNegativeButton(String text, final DialogInterface.OnClickListener listener) {
        setArgument(NEGATIVE_BUTTON_TEXT, text);
        onNegativeButtonClickListener = listener;
        setupView();
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

    private void setupView() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            setupView(dialog);
        }
    }

    private void setupView(@NonNull AlertDialog dialog) {
        if (getArguments() != null && getArguments().getString(TITLE) != null) {
            dialog.setTitle(getArguments().getString(TITLE));
        }

        if (getArguments() != null && getArguments().getString(MESSAGE) != null) {
            ((TextView) dialogView.findViewById(R.id.message)).setText(getArguments().getString(MESSAGE));
        }

        if (getArguments() != null && getArguments().getInt(ICON, -1) != -1) {
            dialog.setIcon(getArguments().getInt(ICON));
        }

        if (getArguments() != null) {
            setCancelable(getArguments().getBoolean(CANCELABLE));
        }

        if (getCancelButtonText() != null) {
            dialog.setButton(BUTTON_NEGATIVE, getCancelButtonText(), (dialog1, which) -> {
                dismiss();
                getOnCancelCallback().cancel();
            });
        } else if (getArguments() != null && getArguments().getString(NEGATIVE_BUTTON_TEXT) != null) {
            dialog.setButton(BUTTON_NEGATIVE, getArguments().getString(NEGATIVE_BUTTON_TEXT), onNegativeButtonClickListener);
        }

        if (getArguments() != null && getArguments().getString(POSITIVE_BUTTON_TEXT) != null) {
            dialog.setButton(BUTTON_POSITIVE, getArguments().getString(POSITIVE_BUTTON_TEXT), onPositiveButtonClickListener);
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

    public boolean isShowing() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }
}
