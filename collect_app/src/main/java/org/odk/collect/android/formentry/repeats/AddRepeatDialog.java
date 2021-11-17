package org.odk.collect.android.formentry.repeats;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import org.odk.collect.android.R;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddRepeatDialog {

    private AddRepeatDialog() {}

    public static void show(Context context, String groupLabel, Listener listener) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(context).create();
        DialogInterface.OnClickListener repeatListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE:
                    listener.onAddRepeatClicked();
                    break;
                case BUTTON_NEGATIVE:
                    listener.onCancelClicked();
                    break;
            }
        };

        alertDialog.setMessage(context.getString(R.string.add_repeat_question,
                groupLabel));

        alertDialog.setButton(BUTTON_POSITIVE, context.getString(R.string.add_repeat),
                repeatListener);
        alertDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.dont_add_repeat),
                repeatListener);

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public interface Listener {
        void onAddRepeatClicked();

        void onCancelClicked();
    }
}
