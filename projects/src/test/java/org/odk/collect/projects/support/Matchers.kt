package org.odk.collect.projects.support

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

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

    fun hasTextInputEditTextError(expectedErrorText: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {}

            public override fun matchesSafely(view: View?): Boolean {
                if (view !is TextInputEditText) {
                    return false
                }
                return expectedErrorText == view.error
            }
        }
    }
}
