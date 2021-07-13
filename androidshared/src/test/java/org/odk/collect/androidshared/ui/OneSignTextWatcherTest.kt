package org.odk.collect.androidshared.ui

import android.widget.EditText
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OneSignTextWatcherTest {

    lateinit var oneSignTextWatcher: OneSignTextWatcher
    lateinit var editText: EditText

    @Before
    fun setup() {
        editText = EditText(InstrumentationRegistry.getInstrumentation().targetContext)
        oneSignTextWatcher = OneSignTextWatcher(editText)
        editText.addTextChangedListener(oneSignTextWatcher)
    }

    @Test
    fun `One character should be accepted`() {
        editText.setText("1")
        assertThat(editText.text.toString(), `is`("1"))
    }

    @Test
    fun `One emoji should be accepted`() {
        editText.setText("\uD83D\uDC22")
        assertThat(editText.text.toString(), `is`("\uD83D\uDC22"))
    }

    @Test
    fun `Longer strings should not be accepted`() {
        editText.setText("12")
        assertThat(editText.text.toString(), `is`(""))
        editText.setText("\uD83D\uDC22\uD83D\uDC22")
        assertThat(editText.text.toString(), `is`(""))
    }
}
