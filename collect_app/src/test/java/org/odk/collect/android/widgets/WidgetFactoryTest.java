package org.odk.collect.android.widgets;

import android.app.Activity;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.WidgetTestActivity;
import org.odk.collect.android.widgets.items.LabelWidget;
import org.odk.collect.android.widgets.items.LikertWidget;
import org.odk.collect.android.widgets.items.ListMultiWidget;
import org.odk.collect.android.widgets.items.ListWidget;
import org.odk.collect.android.widgets.items.SelectMultiImageMapWidget;
import org.odk.collect.android.widgets.items.SelectMultiMinimalWidget;
import org.odk.collect.android.widgets.items.SelectMultiWidget;
import org.odk.collect.android.widgets.items.SelectOneImageMapWidget;
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget;
import org.odk.collect.android.widgets.items.SelectOneWidget;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class WidgetFactoryTest {
    private WidgetFactory widgetFactory;

    @Before
    public void setup() {
        Activity activity = CollectHelpers.buildThemedActivity(WidgetTestActivity.class).get();

        widgetFactory = new WidgetFactory(activity, false, false, null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void testCreatingSelectOneMinimalWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something miNimal something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectOneMinimalWidget.class));
    }

    @Test
    public void testCreatingLikertWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something lIkErt something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(LikertWidget.class));
    }

    @Test
    public void testCreatingSelectOneListNoLabelWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something LisT-nOLabeL something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(ListWidget.class));
        assertThat(((ListWidget) widget).shouldDisplayLabel(), is(false));
    }

    @Test
    public void testCreatingSelectOneListWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something LisT something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(ListWidget.class));
        assertThat(((ListWidget) widget).shouldDisplayLabel(), is(true));
    }

    @Test
    public void testCreatingSelectOneLabelWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something lAbeL something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(LabelWidget.class));
    }

    @Test
    public void testCreatingSelectOneImageMapWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("something imaGe-Map something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectOneImageMapWidget.class));
    }

    @Test
    public void testCreatingSelectOneWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectOneWidget.class));

        prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("lorem ipsum")
                .build();

        widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectOneWidget.class));
    }

    @Test
    public void testCreatingSelectMultipleMinimalWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("something miNimal something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectMultiMinimalWidget.class));
    }

    @Test
    public void testCreatingSelectMultipleListNoLabelWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("something LisT-nOLabeL something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(ListMultiWidget.class));
        assertThat(((ListMultiWidget) widget).shouldDisplayLabel(), is(false));
    }

    @Test
    public void testCreatingSelectMultipleListWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("something LisT something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(ListMultiWidget.class));
        assertThat(((ListMultiWidget) widget).shouldDisplayLabel(), is(true));
    }

    @Test
    public void testCreatingSelectMultipleOneLabelWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("something lAbeL something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(LabelWidget.class));
    }

    @Test
    public void testCreatingSelectMultipleImageMapWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("something imaGe-Map something")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectMultiImageMapWidget.class));
    }

    @Test
    public void testCreatingSelectMultipleWidget() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("")
                .build();

        QuestionWidget widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectMultiWidget.class));

        prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
                .withAppearance("lorem ipsum")
                .build();

        widget = widgetFactory.createWidgetFromPrompt(prompt, null);
        assertThat(widget, instanceOf(SelectMultiWidget.class));
    }
}
