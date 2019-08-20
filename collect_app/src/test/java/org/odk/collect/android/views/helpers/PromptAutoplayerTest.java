package org.odk.collect.android.views.helpers;

import androidx.core.util.Pair;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.Helpers.setupMockReference;

public class PromptAutoplayerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Test
    public void whenPromptHasAutoplayAudio_returnsTrue() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(true));
    }

    @Test
    public void whenPromptHasAutoplayAudio_playsAudio() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        helper.autoplayIfNeeded(prompt);
        verify(audioHelper).play(prompt.getIndex().toString(), "file://reference.mp3");
    }

    @Test
    public void whenPromptHasAutoplayAudio_withDifferentCapitalization_returnsTrue() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "aUdio")
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(true));
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_andReturnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI(null)
                .withAdditionalAttribute("autoplay", "audio")
                .build();
        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_returnsTrue() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        setupMockReference("file://audio1.mp3", "file://reference1.mp3", referenceManager);
        setupMockReference("file://audio2.mp3", "file://reference2.mp3", referenceManager);

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio1.mp3"),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio2.mp3")
                ))
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        assertThat(helper.autoplayIfNeeded(prompt), equalTo(true));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_playsAllAudioInOrder() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        setupMockReference("file://audio1.mp3", "file://reference1.mp3", referenceManager);
        setupMockReference("file://audio2.mp3", "file://reference2.mp3", referenceManager);

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio1.mp3"),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio2.mp3")
                ))
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        helper.autoplayIfNeeded(prompt);
        verify(audioHelper).playInOrder(asList(
                new Clip(prompt.getIndex().toString(), "file://reference.mp3"),
                new Clip(prompt.getIndex().toString() + " 0", "file://reference1.mp3"),
                new Clip(prompt.getIndex().toString() + " 1", "file://reference2.mp3")
        ));
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_andIsSelectOne_playsAllSelectAudioInOrder() throws Exception {
        setupMockReference("file://audio1.mp3", "file://reference1.mp3", referenceManager);
        setupMockReference("file://audio2.mp3", "file://reference2.mp3", referenceManager);

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio1.mp3"),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, "file://audio2.mp3")
                ))
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        helper.autoplayIfNeeded(prompt);
        verify(audioHelper).playInOrder(asList(
                new Clip(prompt.getIndex().toString() + " 0", "file://reference1.mp3"),
                new Clip(prompt.getIndex().toString() + " 1", "file://reference2.mp3")
        ));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_butNoSelectChoiceAudio_playsPromptAudio() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);

        helper.autoplayIfNeeded(prompt);
        verify(audioHelper).play(prompt.getIndex().toString(), "file://reference.mp3");
    }

    @Test // We only support audio autoplaying with the helper right now
    public void whenPromptHasAutoplayVideo_returnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAdditionalAttribute("autoplay", "video")
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }

    @Test
    public void whenPromptHasNoAutoplay_returnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAdditionalAttribute("autoplay", null)
                .build();

        PromptAutoplayer helper = new PromptAutoplayer(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }
}