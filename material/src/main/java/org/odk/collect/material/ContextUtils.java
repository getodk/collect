package org.odk.collect.material;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;

class ContextUtils {

    static int getAttributeValue(Context context, @AttrRes int resId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.data;
    }
}
