package org.odk.collect.testshared

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView

object ViewMatchers {

    @JvmStatic
    fun recyclerView(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(RecyclerView::class.java)
    }

    fun atPositionInRecyclerView(listId: Int, position: Int, childViewId: Int): Matcher<View> {
        return withRecyclerView(listId)
            .atPositionOnView(
                position,
                childViewId
            )
    }
}
