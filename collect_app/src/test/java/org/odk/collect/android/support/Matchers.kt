package org.odk.collect.android.support

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

object Matchers {
    fun isPasswordHidden(): Matcher<View> {
        return object : BoundedMatcher<View, EditText>(EditText::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Password is hidden")
            }

            override fun matchesSafely(editText: EditText): Boolean {
                // returns true if password is hidden
                return editText.transformationMethod is PasswordTransformationMethod
            }
        }
    }
}
