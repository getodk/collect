package odk.hedera.collect.widgets;

import android.app.Application;
import android.view.View;

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
import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.audio.AudioHelper;
import odk.hedera.collect.audio.Clip;
import odk.hedera.collect.formentry.media.AudioHelperFactory;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.injection.config.AppDependencyModule;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import org.odk.hedera.collect.support.MockFormEntryPromptBuilder;
import org.odk.hedera.collect.support.RobolectricHelpers;
import odk.hedera.collect.widgets.base.GeneralSelectOneWidgetTest;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.hedera.collect.support.CollectHelpers.setupFakeReferenceManager;
import static odk.hedera.collect.utilities.WidgetAppearanceUtils.NO_BUTTONS;

/**
 * @author James Knight
 */

public class GridWidgetTest extends GeneralSelectOneWidgetTest<GridWidget> {

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
    public void whenChoicesHaveAudio_andNoButtonsMode_clickingChoice_playsAndStopsAudio() throws Exception {
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

        GridWidget widget = getWidget();

        widget.onItemClick(0);
        verify(audioHelper).play(new Clip("i am index 0", REFERENCES.get(0).second));

        widget.onItemClick(0);
        verify(audioHelper).stop();
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_logsAudioChoiceGridEvent() throws Exception {
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
    public void whenChoicesHaveAudio_clickingChoice_doesNotPlayAudio() throws Exception {
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

        GridWidget widget = getWidget();
        widget.onItemClick(0);

        verify(audioHelper, never()).play(any());
    }

    @NonNull
    @Override
    public GridWidget createWidget() {
        return new GridWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
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