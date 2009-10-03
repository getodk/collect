package org.odk.collect.android.views;

import org.odk.collect.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class TwoTextItemCheckView extends RelativeLayout implements Checkable {

	public TwoTextItemCheckView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public TwoTextItemCheckView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TwoTextItemCheckView(Context context) {
		super(context);
	}

	public boolean isChecked() {
		CheckBox c = (CheckBox) findViewById(R.id.twolinecheckbox);
		return c.isChecked();
	}

	public void setChecked(boolean checked) {
		CheckBox c = (CheckBox) findViewById(R.id.twolinecheckbox);
		c.setChecked(checked);
	}

	public void toggle() {
		CheckBox c = (CheckBox) findViewById(R.id.twolinecheckbox);
		c.setChecked(!c.isChecked());

	}

}
