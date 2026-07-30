package org.odk.collect.android.support.rules

import org.junit.rules.ExternalResource
import org.odk.collect.android.widgets.QuestionWidget

class EnableQuestionWidgetLongPressRule : ExternalResource() {
    override fun before() {
        QuestionWidget.longPressMenuEnabled = true
    }

    override fun after() {
        QuestionWidget.longPressMenuEnabled = false
    }
}
