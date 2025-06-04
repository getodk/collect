/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.feature.formentry

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.core.StringEndsWith
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.application.Collect
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex
import org.odk.collect.android.support.rules.BlankFormTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.widgets.DecimalWidget
import org.odk.collect.android.widgets.IntegerWidget
import org.odk.collect.android.widgets.StringWidget
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.strings.R
import java.io.File
import java.io.IOException

/**
 * Tests that intent groups work as described in https://docs.getodk.org/launch-apps-from-collect/#launching-external-apps-to-populate-multiple-fields
 */
class IntentGroupTest {
    var rule: BlankFormTestRule = BlankFormTestRule(INTENT_GROUP_FORM, "intent-group")

    @JvmField
    @Rule
    var copyFormChain: RuleChain = chain()
        .around(RecordedIntentsRule())
        .around(rule)

    // Verifies that a value given to the label text with form buttonText is used as the button text.
    @Test
    fun buttonName_ShouldComeFromSpecialFormText() {
        onView(withText(R.string.launch_app))
            .check(doesNotExist())
        onView(withText("This is buttonText"))
            .check(matches(isDisplayed()))
    }

    // Verifies that a value given to the label text with form noAppErrorString is used as the toast
    // text if no app is found.
    @Test
    fun appMissingErrorText_ShouldComeFromSpecialFormText() {
        rule.startInFormEntry()
            .clickOnText("This is buttonText")
            .checkIsToastWithMessageDisplayed("This is noAppErrorString")
    }

    @Test
    @Throws(IOException::class)
    fun externalApp_ShouldPopulateFields() {
        // None of the integer/decimal/text questions have associated fields or text
        onView(withId(org.odk.collect.android.R.id.edit_text))
            .check(doesNotExist())
        onView(withId(org.odk.collect.android.R.id.text_view))
            .check(doesNotExist())

        assertImageWidgetWithoutAnswer()
        assertAudioWidgetWithoutAnswer()
        assertVideoWidgetWithoutAnswer()
        assertFileWidgetWithoutAnswer()

        val resultIntent = Intent()

        val imageUri = createTempFile("famous", "jpg")
        val audioUri = createTempFile("sampleAudio", "wav")
        val videoUri = createTempFile("sampleVideo", "mp4")
        val fileUri = createTempFile("fruits", "csv")

        resultIntent.putExtra("questionInteger", "25")
        resultIntent.putExtra("questionDecimal", "46.74")
        resultIntent.putExtra("questionText", "sampleAnswer")
        resultIntent.putExtra("questionImage", imageUri)
        resultIntent.putExtra("questionAudio", audioUri)
        resultIntent.putExtra("questionVideo", videoUri)
        resultIntent.putExtra("questionFile", fileUri)

        val clipData = ClipData.newRawUri(null, null)
        clipData.addItem(ClipData.Item("questionImage", null, imageUri))
        clipData.addItem(ClipData.Item("questionAudio", null, audioUri))
        clipData.addItem(ClipData.Item("questionVideo", null, videoUri))
        clipData.addItem(ClipData.Item("questionFile", null, fileUri))

        resultIntent.clipData = clipData
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intending(CoreMatchers.not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )
        onView(withText("This is buttonText"))
            .perform(scrollTo(), click())

        assertIntegerWidgetWithAnswer()
        assertDecimalWidgetWithAnswer()
        assertStringWidgetWithAnswer()

        assertImageWidgetWithAnswer()
        assertAudioWidgetWithAnswer()
        assertVideoWidgetWithAnswer()
        assertFileWidgetWithAnswer()
    }

    @Test
    fun externalApp_ShouldNotPopulateFieldsIfAnswersAreNull() {
        assertImageWidgetWithoutAnswer()
        assertAudioWidgetWithoutAnswer()
        assertVideoWidgetWithoutAnswer()
        assertFileWidgetWithoutAnswer()

        val resultIntent = Intent()

        resultIntent.putExtra("questionInteger", null as Bundle?)
        resultIntent.putExtra("questionDecimal", null as Bundle?)
        resultIntent.putExtra("questionText", null as Bundle?)
        resultIntent.putExtra("questionImage", null as Bundle?)
        resultIntent.putExtra("questionAudio", null as Bundle?)
        resultIntent.putExtra("questionVideo", null as Bundle?)
        resultIntent.putExtra("questionFile", null as Bundle?)

        val clipData = ClipData.newRawUri(null, null)
        clipData.addItem(ClipData.Item("questionImage", null, null))
        clipData.addItem(ClipData.Item("questionAudio", null, null))
        clipData.addItem(ClipData.Item("questionVideo", null, null))
        clipData.addItem(ClipData.Item("questionFile", null, null))

        resultIntent.clipData = clipData
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intending(CoreMatchers.not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )
        onView(withText("This is buttonText"))
            .perform(scrollTo(), click())

        onView(withIndex(withClassName(StringEndsWith.endsWith("EditText")), 0))
            .check(matches(withText("")))
        onView(withIndex(withClassName(StringEndsWith.endsWith("EditText")), 1))
            .check(matches(withText("")))
        onView(withIndex(withClassName(StringEndsWith.endsWith("EditText")), 2))
            .check(matches(withText("")))

        assertImageWidgetWithoutAnswer()
        assertAudioWidgetWithoutAnswer()
        assertVideoWidgetWithoutAnswer()
        assertFileWidgetWithoutAnswer()
    }

    @Test
    fun collect_shouldNotCrashWhenAnyExceptionIsThrownWhileReceivingAnswer() {
        assertImageWidgetWithoutAnswer()

        val resultIntent = Intent()

        val uri = Mockito.mock(Uri::class.java)
        Mockito.`when`(uri.scheme).thenThrow(RuntimeException())

        resultIntent.putExtra("questionImage", uri)

        intending(CoreMatchers.not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )

        onView(withText("This is buttonText")).perform(click())

        assertImageWidgetWithoutAnswer()
    }

    @Test
    fun collect_shouldNotCrashWhenAnyErrorIsThrownWhileReceivingAnswer() {
        assertImageWidgetWithoutAnswer()

        val resultIntent = Intent()

        val uri = Mockito.mock(Uri::class.java)
        Mockito.`when`(uri.scheme).thenThrow(Error())

        resultIntent.putExtra("questionImage", uri)

        intending(CoreMatchers.not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultIntent
            )
        )

        onView(withText("This is buttonText")).perform(click())

        assertImageWidgetWithoutAnswer()
    }

    private fun assertIntegerWidgetWithAnswer() {
        onView(withText("Integer external")).perform(scrollTo())
            .check(matches(isDisplayed()))
        onView(CoreMatchers.allOf(
            isDescendantOfA(isAssignableFrom(IntegerWidget::class.java)),
            isAssignableFrom(TextInputEditText::class.java)))
            .check(matches(withText("25")))
    }

    private fun assertDecimalWidgetWithAnswer() {
        onView(withText("Decimal external")).perform(scrollTo())
            .check(matches(isDisplayed()))
        onView(CoreMatchers.allOf(
            isDescendantOfA(isAssignableFrom(DecimalWidget::class.java)),
            isAssignableFrom(TextInputEditText::class.java)))
            .check(matches(withText("46.74")))
    }

    private fun assertStringWidgetWithAnswer() {
        onView(withText("Text external")).perform(scrollTo())
            .check(matches(isDisplayed()))
        onView(
            CoreMatchers.allOf(
                isDescendantOfA(CoreMatchers.allOf(
                    isAssignableFrom(StringWidget::class.java),
                    CoreMatchers.not(isAssignableFrom(IntegerWidget::class.java)),
                    CoreMatchers.not(isAssignableFrom(DecimalWidget::class.java)))),
                isAssignableFrom(TextInputEditText::class.java))
        ).check(matches(withText("sampleAnswer")))
    }

    private fun assertImageWidgetWithoutAnswer() {
        onView(CoreMatchers.allOf(
            withTagValue(Matchers.`is`("ImageView")),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(doesNotExist())

        onView(withId(org.odk.collect.android.R.id.capture_button))
            .check(matches(CoreMatchers.not(isDisplayed())))

        onView(withId(org.odk.collect.android.R.id.choose_button))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun assertAudioWidgetWithoutAnswer() {
        onView(withId(org.odk.collect.android.R.id.audio_controller))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun assertVideoWidgetWithoutAnswer() {
        onView(withText(Matchers.`is`("Video external")))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withId(org.odk.collect.android.R.id.play_video_button))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun assertFileWidgetWithoutAnswer() {
        onView(withTagValue(Matchers.`is`("ArbitraryFileWidgetAnswer")))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun assertImageWidgetWithAnswer() {
        onView(CoreMatchers.allOf(
            withTagValue(Matchers.`is`("ImageView")),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(matches(CoreMatchers.not(doesNotExist())))

        onView(withId(org.odk.collect.android.R.id.capture_button))
            .check(matches(CoreMatchers.not(isDisplayed())))

        onView(withId(org.odk.collect.android.R.id.choose_button))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun assertAudioWidgetWithAnswer() {
        onView(withId(org.odk.collect.android.R.id.audio_controller))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    private fun assertVideoWidgetWithAnswer() {
        onView(withId(org.odk.collect.android.R.id.play_video_button))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
        onView(withId(org.odk.collect.android.R.id.play_video_button))
            .check(matches(isEnabled()))
    }

    private fun assertFileWidgetWithAnswer() {
        onView(withTagValue(Matchers.`is`("ArbitraryFileWidgetAnswer")))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Throws(IOException::class)
    private fun createTempFile(name: String, extension: String): Uri {
        // Use the phones downloads dir for temp files
        val downloadsDir = ApplicationProvider
            .getApplicationContext<Context>()
            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val file = File.createTempFile(name, extension, downloadsDir)
        FileUtils.copyFileFromResources(
            "media" + File.separator + name + "." + extension,
            file.path
        )
        return getUriForFile(file)
    }

    private fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            Collect.getInstance(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    companion object {
        private const val INTENT_GROUP_FORM = "intent-group.xml"
    }
}
