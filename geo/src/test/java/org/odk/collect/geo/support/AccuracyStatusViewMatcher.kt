package org.odk.collect.geo.support

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.geo.geopoint.AccuracyStatusView
import org.odk.collect.geo.geopoint.LocationAccuracy

internal class AccuracyStatusViewMatcher(private val accuracy: LocationAccuracy) :
    TypeSafeMatcher<View>() {
    override fun matchesSafely(view: View): Boolean {
        return view is AccuracyStatusView && view.accuracy == accuracy
    }

    override fun describeTo(description: Description) {
        description.appendText("AccuracyStatusView with a accuracy=$accuracy")
    }

    companion object {
        fun hasAccuracy(accuracy: LocationAccuracy): AccuracyStatusViewMatcher {
            return AccuracyStatusViewMatcher(accuracy)
        }
    }
}