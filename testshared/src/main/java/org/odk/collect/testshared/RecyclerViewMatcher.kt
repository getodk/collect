package org.odk.collect.testshared

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// https://github.com/dannyroa/espresso-samples/blob/master/RecyclerView/app/src/androidTest/java/com/dannyroa/espresso_samples/recyclerview/RecyclerViewMatcher.java
class RecyclerViewMatcher private constructor(private val recyclerViewId: Int) {

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null

            override fun describeTo(description: Description) {
                var idDescription = recyclerViewId.toString()
                if (resources != null) {
                    idDescription = try {
                        resources!!.getResourceName(recyclerViewId)
                    } catch (e: Resources.NotFoundException) {
                        String.format("%s (resource name not found)", recyclerViewId)
                    }
                }
                description.appendText("RecyclerView with id: $idDescription at position: $position")
            }

            public override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                if (childView == null) {
                    val recyclerView: RecyclerView = view.rootView.findViewById(
                        recyclerViewId
                    )
                    if (recyclerView.id == recyclerViewId) {
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                        if (viewHolder != null) {
                            childView = viewHolder.itemView
                        }
                    } else {
                        return false
                    }
                }
                return if (targetViewId == -1) {
                    view === childView
                } else {
                    val targetView = childView!!.findViewById<View>(targetViewId)
                    view === targetView
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
            return RecyclerViewMatcher(recyclerViewId)
        }

        @JvmStatic
        fun withListSize(size: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                public override fun matchesSafely(view: View): Boolean {
                    return (view as RecyclerView).adapter!!.itemCount == size
                }

                override fun describeTo(description: Description) {
                    description.appendText("RecyclerView should have $size items")
                }
            }
        }
    }
}
