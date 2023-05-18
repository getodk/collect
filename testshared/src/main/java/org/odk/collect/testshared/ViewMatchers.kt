package org.odk.collect.testshared

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

object ViewMatchers {

    @JvmStatic
    fun recyclerView(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(RecyclerView::class.java)
    }
}
