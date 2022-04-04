package org.odk.collect.testshared

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

object ViewActions {

    @JvmStatic
    fun clickOnViewChild(viewId: Int) = object : ViewAction {
        override fun getConstraints() = null

        override fun getDescription() = "Click on a child view with specified id."

        override fun perform(uiController: UiController, view: View) {
            val v = view.findViewById<View>(viewId)
            v.performClick()
        }
    }
}
