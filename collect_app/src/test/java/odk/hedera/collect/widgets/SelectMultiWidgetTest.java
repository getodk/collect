package odk.hedera.collect.widgets;

import android.app.Application;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

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
import odk.hedera.collect.audio.AudioButton;
import odk.hedera.collect.audio.AudioHelper;
import odk.hedera.collect.audio.Clip;
import odk.hedera.collect.formentry.media.AudioHelperFactory;
import odk.hedera.collect.formentry.questions.AudioVideoImageTextLabel;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.injection.config.AppDependencyModule;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import org.odk.hedera.collect.support.MockFormEntryPromptBuilder;
import org.odk.hedera.collect.support.RobolectricHelpers;
import odk.hedera.collect.utilities.WidgetAppearanceUtils;
import odk.hedera.collect.widgets.base.GeneralSelectMultiWidgetTest;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.hedera.collect.support.CollectHelpers.setupFakeReferenceManager;
import static org.odk.hedera.collect.support.RobolectricHelpers.populateRecyclerView;

/**
 * @author James Knight
 */

public class SelectMultiWidgetTest extends GeneralSelectMultiWidgetTest<SelectMultiWidget> {

    @NonNull
    @Override
    public SelectMultiWidget createWidget() {
        return new SelectMultiWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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

    private void overrideDependencyModule() throws Exception {
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

        AudioVideoImageTextLabel avitLabel = (AudioVideoImageTextLabel) (((RecyclerView) getSpyWidget().answerLayout.getChildAt(0)).getLayoutManager().getChildAt(0));
        assertThat(avitLabel.isEnabled(), is(Boolean.FALSE));

        resetWidget();

        // No-buttons appearance
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withAppearance(WidgetAppearanceUtils.NO_BUTTONS)
                .build();

        populateRecyclerView(getWidget());

        FrameLayout view = (FrameLayout) ((RecyclerView) getSpyWidget().answerLayout.getChildAt(0)).getLayoutManager().getChildAt(0);
        assertThat(view.isEnabled(), is(Boolean.FALSE));
    }

    private static final List<Pair<String, String>> REFERENCES = asList(
            new Pair<>("ref", "file://audio.mp3"),
            new Pair<>("ref1", "file://audio1.mp3")
    );
}
