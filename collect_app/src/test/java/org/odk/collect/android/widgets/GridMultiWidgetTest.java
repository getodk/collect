package org.odk.collect.android.widgets;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_BUTTONS;

/**
 * @author James Knight
 */

public class GridMultiWidgetTest extends GeneralSelectMultiWidgetTest<GridMultiWidget> {

    @NonNull
    @Override
    public GridMultiWidget createWidget() {
        return new GridMultiWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private AudioHelper audioHelper;

    @Mock
    private Analytics analytics;

    @Before
    public void overrideDependencyModule() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(REFERENCES);
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public AudioHelperFactory providesAudioHelperFactory() {
                return context -> audioHelper;
            }

            @Override
            public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
                return analytics;
            }
        });
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_clickingChoice_playsAndStopsAudio() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAppearance(NO_BUTTONS)
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        GridMultiWidget widget = getWidget();

        widget.onItemClick(0);
        verify(audioHelper).play(new Clip("i am index 0", REFERENCES.get(0).second));

        widget.onItemClick(0);
        verify(audioHelper).stop();
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode__logsAudioChoiceGridEvent() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAppearance(NO_BUTTONS)
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        getWidget();

        verify(analytics).logEvent("Prompt", "AudioChoiceGrid", "formAnalyticsID");
    }

    @Test
    public void whenChoicesHaveAudio_clickingChoice_doesNotPlayAudio() {
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

        GridMultiWidget widget = getWidget();
        widget.onItemClick(0);

        verify(audioHelper, never()).play(any());
    }

    @Test
    public void getAnswerShouldReflectWhichSelectionsWereMade() {
        GridMultiWidget widget = getSpyWidget();
        assertNull(widget.getAnswer());

        List<SelectChoice> selectChoices = getSelectChoices();
        List<String> selectedValues = new ArrayList<>();

        boolean atLeastOneSelected = false;

        for (int i = 0; i < widget.selectedItems.size(); i++) {
            boolean shouldBeSelected = random.nextBoolean();
            if (shouldBeSelected) {
                widget.selectedItems.add(i);
            }

            atLeastOneSelected = atLeastOneSelected || shouldBeSelected;

            if (shouldBeSelected) {
                SelectChoice selectChoice = selectChoices.get(i);
                selectedValues.add(selectChoice.getValue());
            }
        }

        // Make sure at least one item is selected, so we're not just retesting the
        // null answer case:
        if (!atLeastOneSelected) {
            int randomIndex = Math.abs(random.nextInt()) % widget.items.size();

            widget.selectedItems.add(randomIndex);
            SelectChoice selectChoice = selectChoices.get(randomIndex);
            selectedValues.add(selectChoice.getValue());
        }

        SelectMultiData answer = (SelectMultiData) widget.getAnswer();

        @SuppressWarnings("unchecked")
        List<Selection> answerSelections = (List<Selection>) answer.getValue();
        List<String> answerValues = selectionsToValues(answerSelections);

        for (String selectedValue : selectedValues) {
            assertTrue(answerValues.contains(selectedValue));
        }
    }

    private List<String> selectionsToValues(List<Selection> selections) {
        List<String> values = new ArrayList<>();
        for (Selection selection : selections) {
            values.add(selection.getValue());
        }

        return values;
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (View view : getSpyWidget().itemViews) {
            assertThat(view.getVisibility(), is(View.VISIBLE));
            assertThat(view.isEnabled(), is(Boolean.FALSE));
        }
    }

    private static final List<Pair<String, String>> REFERENCES = asList(
            new Pair<>("ref", "file://audio.mp3"),
            new Pair<>("ref1", "file://audio1.mp3")
    );
}
