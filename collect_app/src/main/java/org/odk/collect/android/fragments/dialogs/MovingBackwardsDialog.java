package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import org.odk.collect.android.R;

public class MovingBackwardsDialog extends DialogFragment {

    public static final String MOVING_BACKWARDS_DIALOG_TAG = "movingBackwardsDialogTag";

    public interface MovingBackwardsDialogListener {
        void onMovingBackwardsDisabled();
    }

    private MovingBackwardsDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MovingBackwardsDialogListener) {
            listener = (MovingBackwardsDialogListener) context;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.moving_backwards_title)
                .setMessage(R.string.moving_backwards_dialog_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onMovingBackwardsDisabled();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }
}
