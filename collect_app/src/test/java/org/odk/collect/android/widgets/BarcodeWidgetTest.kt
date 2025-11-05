package org.odk.collect.android.widgets

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import net.bytebuddy.utility.RandomString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.barcode.BarcodeWidget
import org.odk.collect.android.widgets.base.QuestionWidgetTest
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.strings.R.string
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

class BarcodeWidgetTest : QuestionWidgetTest<BarcodeWidget, StringData>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private val waitingForDataRegistry = FakeWaitingForDataRegistry()
    private val permissionsProvider = FakePermissionsProvider().apply {
        setPermissionGranted(true)
    }
    private val cameraUtils = mock<CameraUtils>()

    override fun createWidget(): BarcodeWidget {
        return BarcodeWidget(
            composeRule.activity,
            QuestionDetails(formEntryPrompt),
            dependencies,
            waitingForDataRegistry,
            cameraUtils
        ).also {
            composeRule.activity.setContentView(it)
            activity = composeRule.activity
        }
    }

    override fun getNextAnswer(): StringData? {
        return StringData(RandomString.make())
    }

    @Before
    fun setup() {
        formEntryPrompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_BARCODE)
            .build()
    }

    @Test
    fun `The button is hidden in read-only mode`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .build()
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).assertDoesNotExist()
    }

    @Test
    fun `Display the 'Replace Barcode' button if answer is present`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData("blah"))
            .build()
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.replace_barcode)).assertExists()
    }

    @Test
    fun `Display the answer if answer is present`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData("blah"))
            .build()
        createWidget()
        composeRule.onNodeWithText("blah").assertExists()
    }

    @Test
    fun `#getAnswer returns null when there is no answer`() {
        val widget = createWidget()
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun `#getAnswer returns the answer when there is answer`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData("blah"))
            .build()
        val widget = createWidget()
        assertThat(widget.answer!!.displayText, equalTo("blah"))
    }

    @Test
    fun `#clearAnswer removes answer and updates button title`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData("blah"))
            .build()
        val widget = createWidget()
        widget.clearAnswer()

        assertThat(
            widget.answer,
            equalTo(null)
        )
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).assertExists()
    }

    @Test
    fun `#clearAnswer calls #valueChangeListener`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswer(StringData("blah"))
            .build()
        val widget = createWidget()
        val valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `#setData displays sanitized answer`() {
        val widget = createWidget()
        widget.setData("\ud800blah\b")
        assertThat(
            widget.answer!!.displayText,
            equalTo("blah")
        )
    }

    @Test
    fun `#setData updates the button title`() {
        val widget = createWidget()
        widget.setData("\ud800blah\b")
        composeRule.onNodeWithClickLabel(activity.getString(string.replace_barcode)).assertExists()
    }

    @Test
    fun `#setData calls #valueChangeListener`() {
        val widget = createWidget()
        val valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.setData("blah")

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `pressing the button with permission not granted does not launch anything`() {
        val widget = createWidget()
        permissionsProvider.setPermissionGranted(false)
        widget.setPermissionsProvider(permissionsProvider)
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).performClick()

        assertThat(Shadows.shadowOf(activity).nextStartedActivity, equalTo(null))
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true))
    }

    @Test
    fun `pressing the button with permission granted registers widget for data waiting`() {
        whenever(formEntryPrompt.index).thenReturn(formIndex)

        val widget = createWidget()
        widget.setPermissionsProvider(permissionsProvider)
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).performClick()

        assertThat(
            waitingForDataRegistry.waiting.contains(formIndex),
            equalTo(true)
        )
    }

    @Test
    fun `pressing the button when front camera should be used but it is not available displays a toast`() {
        whenever(cameraUtils.isFrontCameraAvailable(ArgumentMatchers.any())).thenReturn(false)
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAppearance(Appearances.FRONT)
            .build()
        val widget = createWidget()
        widget.setPermissionsProvider(permissionsProvider)
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).performClick()

        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo(activity.getString(string.error_front_camera_unavailable))
        )
    }

    @Test
    fun `pressing the button when front camera should be used and it is available launches correct intent`() {
        whenever(cameraUtils.isFrontCameraAvailable(ArgumentMatchers.any())).thenReturn(true)
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAppearance(Appearances.FRONT)
            .build()
        val widget = createWidget()
        widget.setPermissionsProvider(permissionsProvider)
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).performClick()

        assertThat(
            Shadows.shadowOf(activity).nextStartedActivity.getBooleanExtra(Appearances.FRONT, false),
            equalTo(true)
        )
    }

    @Test
    fun `The answer is not displayed with hidden mode`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAppearance(Appearances.HIDDEN_ANSWER)
            .withAnswer(StringData("original contents"))
            .build()

        val widget = createWidget()

        // Check initial value is not shown
        composeRule.onNodeWithClickLabel(activity.getString(string.replace_barcode)).assertExists()
        composeRule.onNodeWithText("original contents").assertDoesNotExist()
        assertThat(widget.answer, equalTo(StringData("original contents")))

        // Check updates aren't shown
        widget.setData("updated contents")
        composeRule.onNodeWithClickLabel(activity.getString(string.replace_barcode)).assertExists()
        composeRule.onNodeWithText("updated contents").assertDoesNotExist()
        assertThat(
            widget.answer,
            equalTo(StringData("updated contents"))
        )
    }

    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .build()
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.get_barcode)).assertDoesNotExist()
    }
}
