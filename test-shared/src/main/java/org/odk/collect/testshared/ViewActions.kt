package org.odk.collect.testshared

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.allViews
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

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
}
