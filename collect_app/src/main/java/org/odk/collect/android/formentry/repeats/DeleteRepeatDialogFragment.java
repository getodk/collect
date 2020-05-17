package org.odk.collect.android.formentry.repeats;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class DeleteRepeatDialogFragment extends DialogFragment {

    protected FormController formController = Collect.getInstance().getFormController();
    protected DeleteRepeatDialogCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DeleteRepeatDialogCallback) {
            callback = (DeleteRepeatDialogCallback) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        String name = formController.getLastRepeatedGroupName();
        int repeatcount = formController.getLastRepeatedGroupRepeatCount();
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getActivity().getString(R.string.delete_repeat_ask));
        alertDialog.setMessage(getActivity().getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE: // yes
                    callback.deleteGroup();
                    break;

                case BUTTON_NEGATIVE: // no
                    callback.onCancelled();
                    break;
            }
            alertDialog.cancel();
            dismiss();
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, getActivity().getString(R.string.discard_group), quitListener);
        alertDialog.setButton(BUTTON_NEGATIVE, getActivity().getString(R.string.delete_repeat_no), quitListener);

        return alertDialog;
    }

    public interface DeleteRepeatDialogCallback {
        void deleteGroup();
        void onCancelled();
    }
}