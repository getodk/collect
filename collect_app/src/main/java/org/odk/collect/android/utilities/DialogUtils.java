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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;

import timber.log.Timber;

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
     * Creates an error dialog on an activity
     *
     * @param errorMsg   The message to show on the dialog box
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
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.ok), errorListener);

        return alertDialog;
    }

    /**
     * It can be a bad idea to interact with Fragment instances. As much as possible we
     * should be using arguments (for static data the dialog needs), Dagger (for dependencies) or
     * ViewModel (for non static data) to get things into fragments so as to avoid crashes or
     * weirdness when they are recreated.
     */
    @Deprecated
    @Nullable
    public static <T extends DialogFragment> T getDialog(Class<T> dialogClass, FragmentManager fragmentManager) {
        return (T) fragmentManager.findFragmentByTag(dialogClass.getName());
    }

    public static <T extends DialogFragment> void showIfNotShowing(Class<T> dialogClass, FragmentManager fragmentManager) {
        showIfNotShowing(dialogClass, null, fragmentManager);
    }

    public static <T extends DialogFragment> void showIfNotShowing(Class<T> dialogClass, @Nullable Bundle args, FragmentManager fragmentManager) {
        showIfNotShowing(createNewInstance(dialogClass, args), dialogClass, fragmentManager);
    }

    public static <T extends DialogFragment> void showIfNotShowing(T newDialog, Class<T> dialogClass, FragmentManager fragmentManager) {
        if (fragmentManager.isStateSaved()) {
            return;
        }

        String tag = dialogClass.getName();
        T existingDialog = (T) fragmentManager.findFragmentByTag(tag);

        if (existingDialog == null) {
            newDialog.show(fragmentManager.beginTransaction(), tag);

            // We need to execute this transaction. Otherwise a follow up call to this method
            // could happen before the Fragment exists in the Fragment Manager and so the
            // call to findFragmentByTag would return null and result in second dialog being show.
            fragmentManager.executePendingTransactions();
        }
    }

    public static void dismissDialog(Class dialogClazz, FragmentManager fragmentManager) {
        DialogFragment existingDialog = (DialogFragment) fragmentManager.findFragmentByTag(dialogClazz.getName());
        if (existingDialog != null) {
            existingDialog.dismissAllowingStateLoss();

            // We need to execute this transaction. Otherwise a next attempt to display a dialog
            // could happen before the Fragment is dismissed in Fragment Manager and so the
            // call to findFragmentByTag would return something (not null) and as a result the
            // next dialog won't be displayed.
            fragmentManager.executePendingTransactions();
        }
    }

    @NotNull
    private static <T extends DialogFragment> T createNewInstance(Class<T> dialogClass, Bundle args) {
        try {
            T instance = dialogClass.newInstance();
            instance.setArguments(args);
            return instance;
        } catch (IllegalAccessException | InstantiationException e) {
            // These would mean we have a non zero arg constructor for a Fragment
            throw new RuntimeException(e);
        }
    }
}
