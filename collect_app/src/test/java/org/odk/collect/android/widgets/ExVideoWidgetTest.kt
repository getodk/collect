package org.odk.collect.android.widgets

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.StringData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.video.ExVideoWidget
import org.odk.collect.androidshared.system.IntentLauncher
import org.robolectric.shadows.ShadowToast
import java.io.File
import java.io.IOException

class ExVideoWidgetTest : FileWidgetTest<ExVideoWidget>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private var fileRequester = mock<FileRequester>()
    private var mediaUtils: MediaUtils = mock<MediaUtils>().apply {
        whenever(isVideoFile(any())).thenReturn(true)
    }

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMediaUtils(intentLauncher: IntentLauncher): MediaUtils {
                return mediaUtils
            }
        })
        activity = composeRule.activity
    }

    override fun getInitialAnswer(): StringData {
        return StringData("video1.mp4")
    }

    override fun getNextAnswer(): StringData {
        return StringData("video2.mp4")
    }

    override fun createWidget(): ExVideoWidget {
        return ExVideoWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            dependencies,
            FakeQuestionMediaManager(),
            FakeWaitingForDataRegistry(),
            fileRequester!!
        ).also {
            composeRule.activity.setContentView(it)
        }
    }

    @Test
    fun whenWidgetCreated_shouldTheLaunchButtonBeVisible() {
        createWidget()
        composeRule.onNodeWithTag("record_video_button").assertExists()
    }

    @Test
    fun whenWidgetCreated_shouldTheButtonHaveProperName() {
        createWidget()
        composeRule.onNodeWithTag("record_video_button").assertTextEquals("Launch")
    }

    @Test
    fun whenThereIsNoAnswer_shouldOnlyLaunchButtonBeVisible() {
        createWidget()
        composeRule.onNodeWithTag("record_video_button").assertExists()
        composeRule.onNodeWithTag("video_widget_answer").assertDoesNotExist()
    }

    @Test
    fun whenThereIsAnswer_shouldBothTheButtonAndTheVideoViewBeVisible() {
        whenever(formEntryPrompt.getAnswerText()).thenReturn(initialAnswer.displayText)
        createWidget()

        composeRule.onNodeWithTag("record_video_button").assertExists()
        composeRule.onNodeWithTag("video_widget_answer").assertExists()
    }

    @Test
    fun whenClearAnswerCall_shouldVideoViewBeHidden() {
        whenever(formEntryPrompt.getAnswerText()).thenReturn(initialAnswer.displayText)

        widget.clearAnswer()
        composeRule.onNodeWithTag("record_video_button").assertExists()
        composeRule.onNodeWithTag("video_widget_answer").assertDoesNotExist()
    }

    @Test
    fun whenCaptureVideoButtonClicked_exWidgetIntentLauncherShouldBeStarted() {
        createWidget()
        composeRule.onNodeWithTag("record_video_button").performClick()
        verify(fileRequester).launch(activity, ApplicationConstants.RequestCodes.EX_VIDEO_CHOOSER, formEntryPrompt)
    }

    @Test
    fun whenClickingOnPlayButton_shouldFileViewerByCalled() {
        whenever(formEntryPrompt.getAnswerText()).thenReturn(initialAnswer.displayText)
        createWidget()
        composeRule.onNodeWithTag("video_widget_answer").performClick()

        verify(mediaUtils).openFile(any(), any(), any<String>())
    }

    @Test
    @Throws(IOException::class)
    fun whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isVideoFile(answer)).thenReturn(false)
        widget.setData(answer)
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    @Throws(IOException::class)
    fun whenUnsupportedFileTypeAttached_shouldTheFileBeRemoved() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isVideoFile(answer)).thenReturn(false)
        widget.setData(answer)
        verify(mediaUtils).deleteMediaFile(answer.absolutePath)
    }

    @Test
    @Throws(IOException::class)
    fun whenUnsupportedFileTypeAttached_shouldToastBeDisplayed() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isVideoFile(answer)).thenReturn(false)
        widget.setData(answer)
        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo("Application returned an invalid file type")
        )
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)
        createWidget()

        composeRule.onNodeWithTag("record_video_button").assertDoesNotExist()
    }

    @Test
    fun whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)
        createWidget()

        composeRule.onNodeWithTag("record_video_button").assertDoesNotExist()
    }
}
