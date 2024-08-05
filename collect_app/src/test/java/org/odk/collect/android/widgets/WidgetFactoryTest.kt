package org.odk.collect.android.widgets

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.Constants
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.items.LabelWidget
import org.odk.collect.android.widgets.items.LikertWidget
import org.odk.collect.android.widgets.items.ListMultiWidget
import org.odk.collect.android.widgets.items.ListWidget
import org.odk.collect.android.widgets.items.SelectMultiImageMapWidget
import org.odk.collect.android.widgets.items.SelectMultiMinimalWidget
import org.odk.collect.android.widgets.items.SelectMultiWidget
import org.odk.collect.android.widgets.items.SelectOneFromMapWidget
import org.odk.collect.android.widgets.items.SelectOneImageMapWidget
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget
import org.odk.collect.android.widgets.items.SelectOneWidget

@RunWith(AndroidJUnit4::class)
class WidgetFactoryTest {
    private val activity: Activity = CollectHelpers.buildThemedActivity(WidgetTestActivity::class.java).get()

    private var widgetFactory = WidgetFactory(
        activity,
        false,
        false,
        null,
        null,
        null,
        null,
        mock<FormEntryViewModel>(),
        null,
        null,
        null,
        null,
        null,
        null,
        mock()
    )

    @Test
    fun testCreatingSelectOneMinimalWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something miNimal something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneMinimalWidget::class.java))
    }

    @Test
    fun testCreatingLikertWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something lIkErt something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(LikertWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneListNoLabelWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something LisT-nOLabeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ListWidget::class.java))
        assertThat((widget as ListWidget).shouldDisplayLabel(), equalTo(false))
    }

    @Test
    fun testCreatingSelectOneListWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something LisT something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ListWidget::class.java))
        assertThat((widget as ListWidget).shouldDisplayLabel(), equalTo(true))
    }

    @Test
    fun testCreatingSelectOneLabelWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something lAbeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(LabelWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneImageMapWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something imaGe-Map something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneImageMapWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneWidget() {
        var prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("")
            .build()

        var widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneWidget::class.java))

        prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("lorem ipsum")
            .build()

        widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleMinimalWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something miNimal something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectMultiMinimalWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleListNoLabelWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something LisT-nOLabeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ListMultiWidget::class.java))
        assertThat((widget as ListMultiWidget).shouldDisplayLabel(), equalTo(false))
    }

    @Test
    fun testCreatingSelectMultipleListWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something LisT something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ListMultiWidget::class.java))
        assertThat((widget as ListMultiWidget).shouldDisplayLabel(), equalTo(true))
    }

    @Test
    fun testCreatingSelectMultipleOneLabelWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something lAbeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(LabelWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleImageMapWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something imaGe-Map something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectMultiImageMapWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleWidget() {
        var prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("")
            .build()

        var widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectMultiWidget::class.java))

        prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("lorem ipsum")
            .build()

        widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectMultiWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneFromMapWidget() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("map")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneFromMapWidget::class.java))
    }

    @Test
    fun exStringWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_TEXT)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ExStringWidget::class.java))
    }

    @Test
    fun exIntegerWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_INTEGER)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ExIntegerWidget::class.java))
    }

    @Test
    fun exDecimalWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_DECIMAL)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(ExDecimalWidget::class.java))
    }

    @Test
    fun testCreatingCounterWidget() {
        listOf("counter", "CouNTer").forEach { appearance ->
            val prompt = MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_INPUT)
                .withDataType(Constants.DATATYPE_INTEGER)
                .withAppearance(appearance)
                .build()

            val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
            assertThat(widget, instanceOf(CounterWidget::class.java))
        }
    }
}
