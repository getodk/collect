package org.odk.collect.android.widgets;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class QuestionWidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public FormEntryPrompt formEntryPrompt;

    @Mock
    public IFormElement formElement;

    @Mock
    public ReferenceManager referenceManager;

    @Before
    public void setup() {
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);
        overrideDependencyModule();
    }

    @Test
    public void whenQuestionHasAudioMedia_playingAudio_highlightsText() throws Exception {
        addAudioToForm(formEntryPrompt);
        TestWidget widget = createTestWidget(formEntryPrompt);

        int originalTextColor = widget.getQuestionMediaLayout().getView_Text().getCurrentTextColor();

        widget.getQuestionMediaLayout().findViewById(R.id.audioButton).performClick();
        int textColor = widget.getQuestionMediaLayout().getView_Text().getCurrentTextColor();
        assertThat(textColor, not(equalTo(originalTextColor)));

        widget.getQuestionMediaLayout().findViewById(R.id.audioButton).performClick();
        textColor = widget.getQuestionMediaLayout().getView_Text().getCurrentTextColor();
        assertThat(textColor, equalTo(originalTextColor));
    }

    private QuestionWidgetTest.TestWidget createTestWidget(FormEntryPrompt formEntryPrompt) {
        FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);
        activity.setTheme(R.style.LightAppTheme); // Needed so attrs are available

        return new TestWidget(activity, formEntryPrompt);
    }

    private void addAudioToForm(FormEntryPrompt formEntryPrompt) throws InvalidReferenceException {
        when(formEntryPrompt.getAudioText()).thenReturn("file://audio.mp3");
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource("file://audio.mp3"), new ShadowMediaPlayer.MediaInfo());
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn("file://audio.mp3");
        when(referenceManager.deriveReference("file://audio.mp3")).thenReturn(reference);
    }

    private void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    private static class TestWidget extends QuestionWidget {

        TestWidget(Context context, FormEntryPrompt prompt) {
            super(context, prompt);
        }

        @Override
        public void setOnLongClickListener(OnLongClickListener l) {

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
