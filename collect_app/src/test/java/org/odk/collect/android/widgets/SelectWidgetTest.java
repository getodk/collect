package org.odk.collect.android.widgets;

import android.content.Context;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.formentry.AudioVideoImageTextLabel;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.Helpers.createMockReference;

@RunWith(RobolectricTestRunner.class)
public class SelectWidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Before
    public void setup() {
        overrideDependencyModule();
        when(audioHelper.setAudio(any(AudioButton.class), any(), any())).thenReturn(new MutableLiveData<>());
    }

    @Test
    public void whenChoicesHaveAudio_audioButtonUsesIndexAsClipID() throws Exception {
        String reference1 = createMockReference(referenceManager, "file://blah1.mp3");
        String reference2 = createMockReference(referenceManager, "file://blah2.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
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

        TestScreenContextActivity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);
        new TestWidget(activity, prompt, audioHelper, prompt.getSelectChoices());

        verify(audioHelper).setAudio(any(AudioButton.class), eq(reference1), eq("i am index 0"));
        verify(audioHelper).setAudio(any(AudioButton.class), eq(reference2), eq("i am index 1"));
    }

    private void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    private static class TestWidget extends SelectWidget {

        TestWidget(Context context, FormEntryPrompt prompt, AudioHelper audioHelper, List<SelectChoice> choices) {
            super(context, prompt, audioHelper);

            for (SelectChoice choice : choices) {
                addMediaFromChoice(
                        new AudioVideoImageTextLabel(context),
                        choices.indexOf(choice),
                        new TextView(context),
                        choices
                );
            }
        }

        @Override
        public IAnswerData getAnswer() {
            return null;
        }

        @Override
        public void clearAnswer() {

        }
    }

}