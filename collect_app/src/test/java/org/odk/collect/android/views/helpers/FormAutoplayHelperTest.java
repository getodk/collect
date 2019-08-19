package org.odk.collect.android.views.helpers;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.audio.AudioHelper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.Helpers.buildMockForm;
import static org.odk.collect.android.support.Helpers.setupMockReference;

public class FormAutoplayHelperTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Test
    public void whenPromptHasAutoplayAudio_returnsTrue() throws Exception {
        setupMockReference("file://audio.mp3", referenceManager);
        FormEntryPrompt prompt = formWithAutoplayOption("audio", "file://audio.mp3");

        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(true));
    }

    @Test
    public void whenPromptHasAutoplayAudio_playsAudio() throws Exception {
        setupMockReference("file://audio.mp3", "file://reference.mp3", referenceManager);
        FormEntryPrompt prompt = formWithAutoplayOption("audio", "file://audio.mp3");

        AudioHelper audioHelper = mock(AudioHelper.class);
        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);

        helper.autoplayIfNeeded(prompt);
        verify(audioHelper).play(prompt.getIndex().toString(), "file://reference.mp3");
    }

    @Test
    public void whenPromptHasAutoplayAudio_withDifferentCapitalization_returnsTrue() throws Exception {
        setupMockReference("file://audio.mp3", referenceManager);
        FormEntryPrompt prompt = formWithAutoplayOption("aUdio", "file://audio.mp3");

        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(true));
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_andReturnsFalse() throws Exception {
        FormEntryPrompt prompt = formWithAutoplayOption("audio", null);
        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);

        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }

    @Test // We only support audio autoplaying with the helper right now
    public void whenPromptHasAutoplayVideo_returnsFalse() {
        FormEntryPrompt prompt = formWithAutoplayOption("video", null);

        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }

    @Test
    public void whenPromptHasNoAutoplay_returnsFalse() {
        FormEntryPrompt prompt = formWithAutoplayOption(null, null);

        FormAutoplayHelper helper = new FormAutoplayHelper(audioHelper, referenceManager);
        assertThat(helper.autoplayIfNeeded(prompt), equalTo(false));
    }

    @NotNull
    private FormEntryPrompt formWithAutoplayOption(String option, String audioURI) {
        FormEntryPrompt prompt = buildMockForm();
        when(prompt.getFormElement().getAdditionalAttribute(null, "autoplay")).thenReturn(option);
        when(prompt.getAudioText()).thenReturn(audioURI);

        return prompt;
    }
}