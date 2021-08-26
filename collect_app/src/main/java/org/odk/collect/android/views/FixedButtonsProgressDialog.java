package org.odk.collect.android.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.odk.collect.android.utilities.ThemeUtils;

/**
 * {@link ProgressDialog} that uses correct theme colors for buttons
 *
 * @deprecated {@link ProgressDialog} is deprecated and should be replaced by
 * {@link androidx.appcompat.app.AlertDialog} or
 * {@link org.odk.collect.android.fragments.dialogs.ProgressDialogFragment}
 */

@Deprecated
public class FixedButtonsProgressDialog extends ProgressDialog {

    public FixedButtonsProgressDialog(Context context) {
        super(context);
        setOnShowListener(dialog -> {
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
        });
    }
}
