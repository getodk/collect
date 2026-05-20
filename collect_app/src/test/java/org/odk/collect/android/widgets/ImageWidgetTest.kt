package org.odk.collect.android.widgets

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
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

    private var currentFile: File? = null
    private lateinit var fileAnswerDelegate: FileAnswerDelegate

    private val fakeQuestionMediaManager = object : FakeQuestionMediaManager() {
        override fun getAnswerFile(fileName: String): File? {
            return currentFile
        }
    }
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), fakeQuestionMediaManager, mock())
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

    override fun createWidget(): ImageWidget {
        fileAnswerDelegate = spy(FileAnswerDelegate(fakeQuestionMediaManager, formEntryPrompt))
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_IMAGE_CHOOSE)

        return ImageWidget(
            composeRule.activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            fakeQuestionMediaManager,
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
        verify(fileAnswerDelegate).deleteFile()
    }

    @Test
    override fun callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer()
        verify(fileAnswerDelegate).deleteFile()
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
        currentFile = File.createTempFile("current", ".bmp")

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText("current.bmp")
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when the answer image cannot be loaded shows error message`() {
        val imageFile = File("non_existent_file.bmp")
        currentFile = imageFile

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText("non_existent_file.bmp")
            .build()

        createWidget()
        composeRule.waitUntilAtLeastOneExists(hasText(activity.getString(string.selected_invalid_image)))
    }
}
