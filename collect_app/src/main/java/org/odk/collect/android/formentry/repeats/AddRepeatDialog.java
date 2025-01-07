package org.odk.collect.android.formentry.repeats;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddRepeatDialog {

    private AddRepeatDialog() {
    }

    public static void show(Context context, String groupLabel, Listener listener) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(context).create();
        DialogInterface.OnClickListener repeatListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE -> listener.onAddRepeatClicked();
                case BUTTON_NEGATIVE -> listener.onCancelClicked();
            }
        };

        String dialogMessage;
        if (groupLabel.isBlank()) {
            dialogMessage = context.getString(org.odk.collect.strings.R.string.add_another_question);
        } else {
            dialogMessage = context.getString(org.odk.collect.strings.R.string.add_repeat_question,
                    groupLabel);
        }

        alertDialog.setTitle(dialogMessage);

        alertDialog.setButton(BUTTON_POSITIVE, context.getString(org.odk.collect.strings.R.string.add_repeat),
                repeatListener);
        alertDialog.setButton(BUTTON_NEGATIVE, context.getString(org.odk.collect.strings.R.string.cancel),
                repeatListener);

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public interface Listener {
        void onAddRepeatClicked();

        void onCancelClicked();
    }
}
