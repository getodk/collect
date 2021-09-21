package org.odk.collect.android.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.odk.collect.android.utilities.ThemeUtils;

/**
 * {@link ProgressDialog} that uses dark or light themes correctly. This will not use correct theme
 * colors/styles however and just use Material defaults instead. Button colors are corrected to
 * prevent them being invisible.
 *
 * @deprecated {@link ProgressDialog} is deprecated and should be replaced by
 * {@link androidx.appcompat.app.AlertDialog} or
 * {@link org.odk.collect.android.fragments.dialogs.ProgressDialogFragment}
 */

@Deprecated
public class DayNightProgressDialog extends ProgressDialog {

    public DayNightProgressDialog(Context context) {
        super(context, new ThemeUtils(context).isDarkTheme() ? android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Material_Light_Dialog_Alert);

        setOnShowListener(dialog -> {
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
        });
    }
}
