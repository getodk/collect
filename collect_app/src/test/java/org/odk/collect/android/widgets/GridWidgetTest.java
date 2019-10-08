package org.odk.collect.android.widgets;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import org.javarosa.core.model.SelectChoice;
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
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.Helpers.createMockReference;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_BUTTONS;

/**
 * @author James Knight
 */

public class GridWidgetTest extends GeneralSelectOneWidgetTest<GridWidget> {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ReferenceManager referenceManager;

    @Mock
    private AudioHelper audioHelper;

    @Mock
    private Analytics analytics;

    @Before
    public void overrideDependencyModule() {
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
            public Analytics providesAnalytics(Application application) {
                return analytics;
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

        GridWidget widget = getActualWidget();

        widget.onItemClick(0);
        verify(audioHelper).play(new Clip("i am index 0", reference));

        widget.onItemClick(0);
        verify(audioHelper).stop();
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_logsAudioChoiceGridEvent() throws Exception {
        createMockReference(referenceManager, "file://blah2.mp3");
        createMockReference(referenceManager, "file://blah1.mp3");

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

        getActualWidget();
        verify(analytics).logEvent("Prompt", "AudioChoiceGrid", "formAnalyticsID");
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

        GridWidget widget = getActualWidget();
        widget.onItemClick(0);

        verify(audioHelper, never()).play(any());
    }

    @NonNull
    @Override
    public GridWidget createWidget() {
        return new GridWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }
}