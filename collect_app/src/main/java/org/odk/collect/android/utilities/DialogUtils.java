/*
 * Copyright 2017 Yura Laguta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.widget.ListView;

import org.odk.collect.android.R;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.AuditEvent;
import org.odk.collect.android.logic.FormController;
import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Reusable code between dialogs for keeping consistency
 */

public final class DialogUtils {

    private DialogUtils() {
    }

    /**
     * List View used with actions
     *
     * @param context UI Context (Activity/Fragment)
     * @return ListView with white divider between items to prevent accidental taps
     */
    @NonNull
    public static ListView createActionListView(@NonNull Context context) {
        int dividerHeight = UiUtils.getDimen(R.dimen.divider_accidental_tap);
        ListView listView = new ListView(context);
        listView.setPadding(0, dividerHeight, 0, 0);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(dividerHeight);
        return listView;
    }

    /**
     * Ensures that a dialog is shown safely and doesn't causes a crash. Useful in the event
     * of a screen rotation, async operations or activity navigation.
     *
     * @param dialog   that needs to be shown
     * @param activity that has the dialog
     */
    public static void showDialog(Dialog dialog, Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (dialog == null || dialog.isShowing()) {
            return;
        }

        try {
            dialog.show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Shows a confirm/cancel dialog for deleting the current repeat group.
     */
    public static void showDeleteRepeatConfirmDialog(Context context, Runnable onDeleted, Runnable onCanceled) {
        FormController formController = Collect.getInstance().getFormController();
        String name = formController.getLastRepeatedGroupName();
        int repeatcount = formController.getLastRepeatedGroupRepeatCount();
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.delete_repeat_ask));
        alertDialog.setMessage(context.getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE: // yes
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.DELETE_REPEAT, true);
                    formController.deleteRepeat();

                    if (onDeleted != null) {
                        onDeleted.run();
                    }

                    break;

                case BUTTON_NEGATIVE: // no
                    if (onCanceled != null) {
                        onCanceled.run();
                    }

                    break;
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, context.getString(R.string.discard_group), quitListener);
        alertDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.delete_repeat_no), quitListener);
        alertDialog.show();
    }

    /**
     * Ensures that a dialog is dismissed safely and doesn't causes a crash. Useful in the event
     * of a screen rotation, async operations or activity navigation.
     *
     * @param dialog   that needs to be shown
     * @param activity that has the dialog
     */
    public static void dismissDialog(Dialog dialog, Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (dialog == null || !dialog.isShowing()) {
            return;
        }

        try {
            dialog.dismiss();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Creates an error dialog on an activity
     *
     * @param errorMsg The message to show on the dialog box
     * @param shouldExit Finish the activity if Ok is clicked
     */
    public static Dialog createErrorDialog(@NonNull Activity activity, String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE:
                    if (shouldExit) {
                        activity.finish();
                    }
                    break;
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(activity.getString(R.string.ok), errorListener);

        return alertDialog;
    }
}
