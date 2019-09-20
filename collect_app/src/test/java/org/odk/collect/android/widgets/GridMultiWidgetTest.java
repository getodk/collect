package org.odk.collect.android.widgets;

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
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.Helpers.createMockReference;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_BUTTONS;

/**
 * @author James Knight
 */

public class GridMultiWidgetTest extends GeneralSelectMultiWidgetTest<GridMultiWidget> {

    @NonNull
    @Override
    public GridMultiWidget createWidget() {
        return new GridMultiWidget(activity, formEntryPrompt, audioHelper);
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ReferenceManager referenceManager;

    @Mock
    private AudioHelper audioHelper;

    @Before
    public void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_clickingChoice_playsAndStopsAudio() throws Exception {
        createMockReference(referenceManager, "file://blah2.mp3");
        String reference = createMockReference(referenceManager, "file://blah1.mp3");

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAppearance(NO_BUTTONS)
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://blah1.mp3"),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://blah2.mp3")
                ))
                .build();

        GridMultiWidget widget = getActualWidget();

        widget.onItemClick(0);
        verify(audioHelper).play(new Clip("i am index 0", reference));

        widget.onItemClick(0);
        verify(audioHelper).stop();
    }

    @Test
    public void whenChoicesHaveAudio_clickingChoice_doesNotPlayAudio() throws Exception {
        createMockReference(referenceManager, "file://blah2.mp3");
        createMockReference(referenceManager, "file://blah1.mp3");

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://blah1.mp3"),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://blah2.mp3")
                ))
                .build();

        GridMultiWidget widget = getActualWidget();
        widget.onItemClick(0);

        verify(audioHelper, never()).play(any());
    }

    @Test
    public void getAnswerShouldReflectWhichSelectionsWereMade() {
        GridMultiWidget widget = getWidget();
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
}
