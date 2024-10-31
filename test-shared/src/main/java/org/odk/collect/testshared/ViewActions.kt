package org.odk.collect.testshared

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.RatingBar
import androidx.annotation.StringRes
import androidx.core.view.allViews
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import org.hamcrest.Matcher

object ViewActions {

    @JvmStatic
    fun clickOnViewContentDescription(@StringRes stringId: Int, context: Context) = object : ViewAction {
        override fun getConstraints() = null

        override fun getDescription() = "Click on a child view with specified content description."

        override fun perform(uiController: UiController, view: View) {
            for (child in (view as ViewGroup).allViews) {
                if (child.contentDescription == context.getString(stringId)) {
                    child.performClick()
                    break
                }
            }
        }
    }

    @JvmStatic
    fun clickOnItemWith(matcher: Matcher<View>): ViewAction {
        return actionOnItem<ViewHolder>(hasDescendant(matcher), click())
    }

    fun scrollNumberPickerToValue(targetValue: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(NumberPicker::class.java)
            }

            override fun getDescription(): String {
                return "Scroll the NumberPicker to a specific value"
            }

            override fun perform(uiController: UiController, view: View) {
                val numberPicker = view as NumberPicker

                while (targetValue != numberPicker.value) {
                    if (targetValue < numberPicker.value) {
                        swipeDown(uiController, view)
                    } else {
                        swipeUp(uiController, view)
                    }
                }
            }

            private fun swipeUp(uiController: UiController, view: View) {
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.CENTER,
                    GeneralLocation.TOP_CENTER,
                    Press.FINGER
                ).perform(uiController, view)
            }

            private fun swipeDown(uiController: UiController, view: View) {
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.CENTER,
                    GeneralLocation.BOTTOM_CENTER,
                    Press.FINGER
                ).perform(uiController, view)
            }
        }
    }

    @JvmStatic
    fun setRating(rating: Float): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(RatingBar::class.java)
            }

            override fun getDescription(): String {
                return "Custom view action to set rating on RatingBar"
            }

            override fun perform(uiController: UiController, view: View) {
                val ratingBar = view as RatingBar
                ratingBar.rating = rating
            }
        }
    }
}
