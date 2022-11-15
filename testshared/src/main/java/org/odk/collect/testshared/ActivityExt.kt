package org.odk.collect.testshared

import android.app.Activity
import android.view.View
import android.view.ViewGroup

object ActivityExt {

    @Suppress("UNCHECKED_CAST")
    fun <T : View> Activity.getContextView(): T {
        return this.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as T
    }
}
