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
import android.support.annotation.NonNull;
import android.widget.ListView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import timber.log.Timber;

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
        Collect.getInstance().getActivityLogger().logAction(activity, "createErrorDialog", "show");
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this,
                                "createErrorDialog", "OK");
                        if (shouldExit) {
                            activity.finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(activity.getString(R.string.ok), errorListener);

        return alertDialog;
    }
}
