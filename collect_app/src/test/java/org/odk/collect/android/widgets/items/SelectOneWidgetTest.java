package org.odk.collect.android.widgets.items;

import android.app.Application;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.flexbox.FlexboxLayoutManager;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.QuestionTextSizeHelper;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;
import org.odk.collect.async.Scheduler;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.odk.collect.android.support.RobolectricHelpers.populateRecyclerView;

/**
 * @author James Knight
 */
public class SelectOneWidgetTest extends GeneralSelectOneWidgetTest<SelectOneWidget> {

    @NonNull
    @Override
    public SelectOneWidget createWidget() {
        SelectOneWidget selectOneWidget = new SelectOneWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
        selectOneWidget.onAttachedToWindow();
        return selectOneWidget;
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public AudioHelper audioHelper;

    @Mock
    public Analytics analytics;

    @Before
    public void setup() throws Exception {
        overrideDependencyModule();
        when(audioHelper.setAudio(any(AudioButton.class), any())).thenReturn(new MutableLiveData<>());
    }

    @Test
    public void byDefault_shouldGridLayoutManagerBeUsed() {
        assertThat(getWidget().binding.choicesRecyclerView.getLayoutManager().getClass().getName(), is(GridLayoutManager.class.getName()));
    }

    @Test
    public void whenColumnsPackAppearanceExist_shouldFlexboxLayoutManagerBeUsed() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-pack");
        assertThat(getWidget().binding.choicesRecyclerView.getLayoutManager().getClass().getName(), is(FlexboxLayoutManager.class.getName()));
    }

    @Test
    public void whenButtonsModeExist_shouldFrameLayoutBeUsedAsItemView() {
        populateRecyclerView(getWidget());
        assertThat(getChoiceView(getWidget(), 0).getClass().getName(), is(AudioVideoImageTextLabel.class.getName()));
    }

    @Test
    public void whenNoButtonsModeExist_shouldFrameLayoutBeUsedAsItemView() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("no-buttons");
        populateRecyclerView(getWidget());
        assertThat(getChoiceView(getWidget(), 0).getClass().getName(), is(FrameLayout.class.getName()));
    }

    @Test
    public void whenAutocompleteAppearanceExist_shouldTextSizeBeSetProperly() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("autocomplete");
        assertThat(getSpyWidget().binding.choicesSearchBox.getTextSize(), is(new QuestionTextSizeHelper().getHeadline6()));
    }

    @Test
    public void whenAutocompleteAppearanceExist_shouldSearchBoxBeVisible() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("autocomplete");
        assertThat(getSpyWidget().binding.choicesSearchBox.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenAutocompleteAppearanceDoesNotExist_shouldSearchBoxBeHidden() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
        assertThat(getSpyWidget().binding.choicesSearchBox.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenChoicesFiltered_shouldProperValuesBeReturnedInButtonsMode() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("AAA", "AAA"),
                        new SelectChoice("BBB", "BBB")
                ))
                .withAppearance("autocomplete")
                .withSelectChoiceText(asList("AAA", "BBB"))
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        assertVisibleItemsInButtonsMode(widget, "AAA", "BBB");
        widget.binding.choicesSearchBox.setText("b");
        assertVisibleItemsInButtonsMode(widget, "BBB");
        widget.binding.choicesSearchBox.setText("bc");
        assertVisibleItemsInButtonsMode(widget);
        widget.binding.choicesSearchBox.setText("b");
        assertVisibleItemsInButtonsMode(widget, "BBB");
        widget.binding.choicesSearchBox.setText("");
        assertVisibleItemsInButtonsMode(widget, "AAA", "BBB");
    }

    @Test
    public void whenChoicesFiltered_shouldProperValuesBeReturnedInNoButtonsMode() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("AAA", "AAA"),
                        new SelectChoice("BBB", "BBB")
                ))
                .withAppearance("autocomplete no-buttons")
                .withSelectChoiceText(asList("AAA", "BBB"))
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        assertVisibleItemsInNoButtonsMode(widget, "AAA", "BBB");
        widget.binding.choicesSearchBox.setText("b");
        assertVisibleItemsInNoButtonsMode(widget, "BBB");
        widget.binding.choicesSearchBox.setText("bc");
        assertVisibleItemsInNoButtonsMode(widget);
        widget.binding.choicesSearchBox.setText("b");
        assertVisibleItemsInNoButtonsMode(widget, "BBB");
        widget.binding.choicesSearchBox.setText("");
        assertVisibleItemsInNoButtonsMode(widget, "AAA", "BBB");
    }

    @Test
    public void whenClickOneElementTwice_shouldThatElementRemainSelectedInNoButtonsMode() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("AAA", "AAA"),
                        new SelectChoice("BBB", "BBB")
                ))
                .withAppearance("no-buttons")
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        clickChoice(widget, 0); // Select AAA
        assertThat(widget.getAnswer().getDisplayText(), is("AAA"));
        assertThat(isItemSelected(widget, 0), is(true));
        assertThat(isItemSelected(widget, 1), is(false));

        clickChoice(widget, 0); // Select AAA again
        assertThat(widget.getAnswer().getDisplayText(), is("AAA"));
        assertThat(isItemSelected(widget, 0), is(true));
        assertThat(isItemSelected(widget, 1), is(false));
    }

    @Test
    public void whenCLickOneOption_shouldPreviouslySelectedOptionBeUnselectedInButtonsMode() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("AAA", "AAA"),
                        new SelectChoice("BBB", "BBB")
                ))
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        clickChoice(widget, 0); // Select AAA
        assertThat(widget.getAnswer().getDisplayText(), is("AAA"));
        assertThat(isItemSelected(widget, 0), is(true));
        assertThat(isItemSelected(widget, 1), is(false));

        clickChoice(widget, 1); // Select BBB
        assertThat(widget.getAnswer().getDisplayText(), is("BBB"));
        assertThat(isItemSelected(widget, 0), is(false));
        assertThat(isItemSelected(widget, 1), is(true));
    }

    @Test
    public void whenClickOneOption_shouldPreviouslySelectedOptionBeUnselectedInNoMode() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("AAA", "AAA"),
                        new SelectChoice("BBB", "BBB")
                ))
                .withAppearance("no-buttons")
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        clickChoice(widget, 0); // Select AAA
        assertThat(widget.getAnswer().getDisplayText(), is("AAA"));
        assertThat(isItemSelected(widget, 0), is(true));
        assertThat(isItemSelected(widget, 1), is(false));

        clickChoice(widget, 1); // Select BBB
        assertThat(widget.getAnswer().getDisplayText(), is("BBB"));
        assertThat(isItemSelected(widget, 0), is(false));
        assertThat(isItemSelected(widget, 1), is(true));
    }

    @Test
    public void whenChoicesHaveAudio_audioButtonUsesIndexAsClipID() throws Exception {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        populateRecyclerView(getWidget());
        verify(audioHelper).setAudio(any(AudioButton.class), eq(new Clip("i am index 0", REFERENCES.get(0).second)));
        verify(audioHelper).setAudio(any(AudioButton.class), eq(new Clip("i am index 1", REFERENCES.get(1).second)));
    }

    @Test
    public void whenChoicesHaveAudio_logsAudioChoiceEvent() throws Exception {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        populateRecyclerView(getWidget());
        verify(analytics).logEvent("Prompt", "AudioChoice", "formAnalyticsID");
    }

    @Test
    public void whenAChoiceValueIsNull_selecting_doesNotSetAnswer() {
        SelectChoice selectChoice = new SelectChoice(); // The two arg constructor protects against null values
        selectChoice.setTextID("1");

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(selectChoice))
                .build();

        SelectOneWidget widget = getWidget();
        populateRecyclerView(widget);

        clickChoice(widget, 0);
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        // No appearance
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withReadOnly(true)
                .build();

        populateRecyclerView(getWidget());

        AudioVideoImageTextLabel avitLabel = (AudioVideoImageTextLabel) getSpyWidget().binding.choicesRecyclerView.getLayoutManager().getChildAt(0);
        assertThat(avitLabel.isEnabled(), is(Boolean.FALSE));

        resetWidget();

        // No-buttons appearance
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withAppearance(WidgetAppearanceUtils.NO_BUTTONS)
                .build();

        populateRecyclerView(getWidget());

        FrameLayout view = (FrameLayout) getSpyWidget().binding.choicesRecyclerView.getLayoutManager().getChildAt(0);
        assertThat(view.isEnabled(), is(Boolean.FALSE));
    }

    private void overrideDependencyModule() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(REFERENCES);
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public AudioHelperFactory providesAudioHelperFactory(Scheduler scheduler) {
                return context -> audioHelper;
            }

            @Override
            public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
                return analytics;
            }
        });
    }

    private boolean isItemSelected(SelectOneWidget widget, int index) {
        return WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt)
                ? isNoButtonItemSelected(widget, index)
                : isButtonItemSelected(widget, index);
    }

    private boolean isNoButtonItemSelected(SelectOneWidget widget, int index) {
        return getChoiceView(widget, index).getBackground() != null;
    }

    private boolean isButtonItemSelected(SelectOneWidget widget, int index) {
        return getRadioButton(widget, index).isChecked();
    }

    private void clickChoice(SelectOneWidget widget, int index) {
        if (WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt)) {
            clickNoButtonChoice(widget, index);
        } else {
            clickButtonChoice(widget, index);
        }
    }

    private void clickNoButtonChoice(SelectOneWidget widget, int index) {
        widget.binding.choicesRecyclerView.getChildAt(index).performClick();
    }

    private void clickButtonChoice(SelectOneWidget widget, int index) {
        ((AudioVideoImageTextLabel) getChoiceView(widget, index)).getLabelTextView().performClick();
    }

    private ViewGroup getChoiceView(SelectOneWidget widget, int index) {
        return (ViewGroup) widget.binding.choicesRecyclerView.getChildAt(index);
    }

    private AudioVideoImageTextLabel getAudioVideoImageTextLabelView(SelectOneWidget widget, int index) {
        return (AudioVideoImageTextLabel) widget.binding.choicesRecyclerView.getChildAt(index);
    }

    private RadioButton getRadioButton(SelectOneWidget widget, int index) {
        return (RadioButton) getAudioVideoImageTextLabelView(widget, index).getLabelTextView();
    }

    private void assertVisibleItemsInButtonsMode(SelectOneWidget widget, String... items) {
        assertThat(getWidget().binding.choicesRecyclerView.getAdapter().getItemCount(), is(items.length));
        int index = 0;
        for (SelectChoice item : getVisibleItems()) {
            assertThat(getRadioButton(widget, getAllItems().indexOf(item)).getText().toString(), is(items[index]));
            index++;
        }
    }

    private void assertVisibleItemsInNoButtonsMode(SelectOneWidget widget, String... items) {
        assertThat(getWidget().binding.choicesRecyclerView.getAdapter().getItemCount(), is(items.length));
        int index = 0;
        for (SelectChoice item : getVisibleItems()) {
            TextView label = (TextView) getChoiceView(widget, getAllItems().indexOf(item)).getChildAt(0);
            assertThat(label.getText(), is(items[index]));
            index++;
        }
    }

    private List<SelectChoice> getVisibleItems() {
        return ((SelectOneListAdapter) getWidget().binding.choicesRecyclerView.getAdapter())
                .getProps()
                .getFilteredItems();
    }

    private List<SelectChoice> getAllItems() {
        return ((SelectOneListAdapter) getWidget().binding.choicesRecyclerView.getAdapter())
                .getProps()
                .getItems();
    }

    private static final List<Pair<String, String>> REFERENCES = asList(
            new Pair<>("ref", "file://audio.mp3"),
            new Pair<>("ref1", "file://audio1.mp3")
    );
}