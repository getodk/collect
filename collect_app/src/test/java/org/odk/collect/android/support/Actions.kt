package org.odk.collect.android.support

import android.view.View
import android.widget.NumberPicker
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

object Actions {
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
}
