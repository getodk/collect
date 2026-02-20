package org.odk.collect.android.widgets

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.arbitraryfile.ArbitraryFileWidget
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.strings.R.string
import java.io.File

class ArbitraryFileWidgetTest : FileWidgetTest<ArbitraryFileWidget>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private val mediaUtils = mock<MediaUtils>().also {
        whenever(it.isAudioFile(any())).thenReturn(true)
    }
    private val questionMediaManager = FakeQuestionMediaManager()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMediaUtils(intentLauncher: IntentLauncher): MediaUtils {
                return mediaUtils
            }
        })
        formEntryPrompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_FILE_CAPTURE)
            .build()
    }

    override fun getInitialAnswer(): StringData {
        return StringData("document.pdf")
    }

    override fun getNextAnswer(): StringData {
        return StringData("document.xlsx")
    }

    override fun createWidget(): ArbitraryFileWidget {
        return ArbitraryFileWidget(
            composeRule.activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            dependencies,
            questionMediaManager,
            FakeWaitingForDataRegistry()
        ).also {
            widgetInComposeActivity(composeRule, it)
            activity = composeRule.activity
        }
    }

    @Test
    fun `Display the answer text when there is answer`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData(initialAnswer.displayText))
            .build()
        createWidget()
        composeRule.onNodeWithText(initialAnswer.displayText).assertExists()
    }

    @Test
    fun `File picker should be called when clicking on button`() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_file)).performClick()
        verify(mediaUtils).pickFile(activity, "*/*", ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER)
    }

    @Test
    fun `File viewer should be called when clicking on answer`() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("document", ".pdf"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData(file.name))
            .build()

        createWidget()
        composeRule.onNodeWithText(file.name).performClick()
        verify(mediaUtils).openFile(activity, file, null)
    }

    @Test
    fun `Hide the answer when clear answer is called`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData(initialAnswer.displayText))
            .build()

        val widget = createWidget()
        widget.clearAnswer()
        composeRule.onNodeWithText(initialAnswer.displayText).assertDoesNotExist()
    }

    @Test
    fun `All clickable elements should be disabled when read-only override option is used`() {
        readOnlyOverride = true
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(false)
            .withAnswer(StringData(initialAnswer.displayText))
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_file)).assertDoesNotExist()
        composeRule.onNodeWithText(initialAnswer.displayText).assertExists()
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .withAnswer(StringData(initialAnswer.displayText))
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_file)).assertDoesNotExist()
        composeRule.onNodeWithText(initialAnswer.displayText).assertExists()
    }

    @Test
    override fun settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("document", ".pdf"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData(file.name))
            .build()

        val widget = createWidget()
        widget.setData(createBinaryData(nextAnswer))

        assertThat(
            questionMediaManager.originalFiles[formEntryPrompt.index.toString()],
            equalTo(file.absolutePath)
        )
    }
}
