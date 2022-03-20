package org.odk.collect.android.widgets.items

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapWidgetTest {

    @Test
    fun clickingButton_opensSelectOneFromMapDialog() {
        val activity = Robolectric.setupActivity(WidgetTestActivity::class.java)
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(null)))

        widget.binding.button.performClick()

        val expectedDialog = getFragmentByClass(
            activity.supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(expectedDialog, notNullValue())
    }
}
