package org.odk.collect.android.views.layout;

import android.util.Log;

public class GroupLayoutFactory {
	
	public static final String FIELD_LIST_APPEARANCE = "field-list";
	public static final String CONDITIONAL_FIELD_LIST_APPEARANCE = "conditional-field-list";

	public static final IGroupLayout createGroupLayout(String appearance) {
		if (FIELD_LIST_APPEARANCE.equals(appearance) ) {
			return new FieldList();
		} else if ( CONDITIONAL_FIELD_LIST_APPEARANCE.equals(appearance) ) {
			return new ConditionalFieldList();
		} else {
			// this is an error!
			Log.e(GroupLayoutFactory.class.getName(), "unrecognized group appearance!");
			return null;
		}
	}
}
