package org.odk.collect.android.widgets

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.imageDecoderEnabled
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.utilities.FileAnswerDelegate
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.robolectric.Shadows.shadowOf
import java.io.File

class ImageWidgetTest : FileWidgetTest<ImageWidget>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private lateinit var fileAnswerDelegate: FileAnswerDelegate
    private val questionMediaManager = FakeQuestionMediaManager()
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), questionMediaManager, mock())
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

    @Before
    fun setup() {
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .imageDecoderEnabled(false)
                .build()
        }
    }

    @After
    fun teardown() {
        SingletonImageLoader.reset()
    }

    override fun createWidget(): ImageWidget {
        fileAnswerDelegate = FileAnswerDelegate(questionMediaManager, formEntryPrompt)
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_IMAGE_CHOOSE)

        return ImageWidget(
            composeRule.activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            TempFiles.getPathInTempDir(),
            dependencies,
            fileAnswerDelegate
        ).also {
            widgetInComposeActivity(composeRule, it)
            activity = composeRule.activity
        }
    }

    @Test
    override fun settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        super.settingANewAnswerShouldRemoveTheOldAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(formEntryPrompt.answerText))
        assertThat(questionMediaManager.recentFiles[promptIndex], notNullValue())
    }

    @Test
    override fun callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(formEntryPrompt.answerText))
        assertThat(questionMediaManager.recentFiles[promptIndex], equalTo(null))
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Test
    fun `buttons should launch correct intents when there is no custom package`() {
        stubAllRuntimePermissionsGranted(true)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        var intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent!!.getPackage(), equalTo(null))

        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).performClick()
        intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `buttons should launch correct intents when custom package is set`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAdditionalAttribute("intent", "com.customcameraapp")
            .build()

        stubAllRuntimePermissionsGranted(true)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        var intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent!!.getPackage(), equalTo("com.customcameraapp"))

        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).performClick()
        intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `capture button should not launch any intent when permissions denied`() {
        stubAllRuntimePermissionsGranted(false)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        assertThat(shadowOf(activity).nextStartedActivity, equalTo(null))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertDoesNotExist()
    }

    @Test
    fun `when read-only override option is used should all clickable elements be disabled`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertDoesNotExist()
    }

    @Test
    fun `when there is no answer hide image view`() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
    }

    @Test
    fun `when prompt has current answer shows in image view`() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(file.name)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when the answer image cannot be loaded shows error message`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText("non_existent_file.bmp")
            .build()

        createWidget()
        composeRule.waitUntilAtLeastOneExists(hasText(activity.getString(string.selected_invalid_image)))
    }
}
