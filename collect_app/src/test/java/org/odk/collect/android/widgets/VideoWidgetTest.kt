package org.odk.collect.android.widgets

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import net.bytebuddy.utility.RandomString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.video.VideoWidget
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.shared.TempFiles.createTempDir
import org.odk.collect.shared.TempFiles.createTempFile
import org.odk.collect.strings.R.string
import org.robolectric.Shadows

class VideoWidgetTest : FileWidgetTest<VideoWidget>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private var destinationName: String? = null
    private var questionMediaManager = mock<QuestionMediaManager>()
    private var mediaUtils = mock<MediaUtils>()
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), questionMediaManager, mediaUtils)
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

    override fun createWidget(): VideoWidget {
        return VideoWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            dependencies,
            FakeQuestionMediaManager(),
            FakeWaitingForDataRegistry()
        ).also {
            composeRule.activity.setContentView(it)
        }
    }

    override fun getNextAnswer(): StringData {
        return StringData(destinationName!!)
    }

    @Before
    override fun setUp() {
        super.setUp()
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_VIDEO_CAPTURE)
        destinationName = RandomString.make()
        activity = composeRule.activity
    }

    @Test
    fun buttonsShouldLaunchCorrectIntents() {
        createWidget()
        stubAllRuntimePermissionsGranted(true)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_video)).performClick()
        var intent = Shadows.shadowOf(activity).nextStartedActivity
        assertActionEquals(MediaStore.ACTION_VIDEO_CAPTURE, intent)

        composeRule.onNodeWithClickLabel(activity.getString(string.choose_video)).performClick()
        intent = Shadows.shadowOf(activity).nextStartedActivity
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("video/*", intent)

        val newFile = createTempFile(createTempDir())
        getWidget()!!.setData(newFile)
        whenever(questionMediaManager.getAnswerFile(newFile.name)).thenReturn(newFile)

        composeRule.onNodeWithClickLabel(activity.getString(string.play_video)).performClick()
        verify(mediaUtils).openFile(any(), any(), any<String>())
    }

    @Test
    fun buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        createWidget()
        stubAllRuntimePermissionsGranted(false)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_video)).performClick()
        assertThat(Shadows.shadowOf(activity).nextStartedActivity, equalTo(null))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_video)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_video)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.play_video)).assertDoesNotExist()
    }

    @Test
    fun whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_video)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_video)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.play_video)).assertDoesNotExist()
    }
}
