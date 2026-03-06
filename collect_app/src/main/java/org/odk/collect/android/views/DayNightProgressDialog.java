package org.odk.collect.android.views;

import static org.odk.collect.androidshared.system.ContextExt.isDarkTheme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.material.MaterialProgressDialogFragment;

/**
 * {@link ProgressDialog} that uses dark or light themes correctly. This will not use correct theme
 * colors/styles however and just use Material defaults instead. Button colors are corrected to
 * prevent them being invisible.
 *
 * @deprecated {@link ProgressDialog} is deprecated and should be replaced by
 * {@link androidx.appcompat.app.AlertDialog} or
 * {@link MaterialProgressDialogFragment}
 */

@Deprecated
public class DayNightProgressDialog extends ProgressDialog {

    public DayNightProgressDialog(Context context) {
        super(context, isDarkTheme(context) ? android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Material_Light_Dialog_Alert);

        setOnShowListener(dialog -> {
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
            ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(new ThemeUtils(((ProgressDialog) dialog).getContext()).getColorPrimary());
        });
    }
}
