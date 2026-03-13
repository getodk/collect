package org.odk.collect.testshared

import android.view.View
import android.widget.NumberPicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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

    fun hasPicked(number: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun matchesSafely(view: View): Boolean {
                return view is NumberPicker && view.displayedValues[view.value] == number
            }

            override fun describeTo(description: Description) {
                description.appendText("is NumberPicker with picked value: ")
                description.appendValue(number)
            }
        }
    }
}
