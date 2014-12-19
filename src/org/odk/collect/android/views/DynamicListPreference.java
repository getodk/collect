package org.odk.collect.android.views;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class DynamicListPreference extends ListPreference {

	// control whether dialog should show or not
	private boolean showDialog = false;

	public DynamicListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicListPreference(Context context) {
		super(context);
	}

	public DynamicListPreference(Context context, boolean show) {
		super(context);
		showDialog = show;
	}

	// This is just to simulate that the user 'clicked' on the preference.
	public void show() {
		showDialog(null);
	}
	
	public boolean shouldShow() {
		return showDialog;
	}

	@Override
	protected void showDialog(Bundle state) {
		if (showDialog) {
			super.showDialog(state);
		} else {
			// we don't want the dialog to show sometimes
			// like immediately after click, so we don't until we've generated
			// the
			// list choices
			return;
		}
	}

	public void setShowDialog(boolean show) {
		showDialog = show;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);

		// causes list to refresh next time dialog is requested
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			setShowDialog(false);
		}
	}

	
	

}
