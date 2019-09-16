package org.odk.collect.android.views.helpers;

import androidx.core.util.Pair;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.media.PromptAutoplayer;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.Helpers.createMockReference;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.COMPACT;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MINIMAL;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_BUTTONS;

public class PromptAutoplayerTest {

    public static final String FORM_IDENTIFIER_HASH = "formIdentifierHash";
    private ReferenceManager referenceManager;
    private AudioHelper audioHelper;
    private Analytics analytics;

    private PromptAutoplayer autoplayer;

    @Before
    public void setup() {
        referenceManager = mock(ReferenceManager.class);
        audioHelper = mock(AudioHelper.class);
        analytics = mock(Analytics.class);

        autoplayer = new PromptAutoplayer(audioHelper, referenceManager, analytics, FORM_IDENTIFIER_HASH);
    }

    @Test
    public void whenPromptHasAutoplayAudio_playsAudio() throws Exception {
        String reference = createMockReference(referenceManager, "file://audio.mp3");
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test
    public void whenPromptHasAutoplayAudio_logsAutoplayAudioLabelEvent() throws Exception {
        createMockReference(referenceManager, "file://audio.mp3");
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .build();

        autoplayer.autoplayIfNeeded(prompt);
        verify(analytics).logEvent("Prompt", "AutoplayAudioLabel", FORM_IDENTIFIER_HASH);
    }

    @Test
    public void whenPromptHasAutoplayAudio_withDifferentCapitalization_playsAudio() throws Exception {
        String reference = createMockReference(referenceManager, "file://audio.mp3");
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "aUdio")
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_returnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAudioURI(null)
                .withAdditionalAttribute("autoplay", "audio")
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(false));
        verify(audioHelper, never()).playInOrder(any());
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_playsAudioInOrder() throws Exception {
        String reference1 = createMockReference(referenceManager, "file://audio.mp3");
        String reference2 = createMockReference(referenceManager, "file://audio1.mp3");
        String reference3 = createMockReference(referenceManager, "file://audio2.mp3");

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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(
                new Clip(prompt.getIndex().toString(), reference1),
                new Clip(prompt.getIndex().toString() + " 0", reference2),
                new Clip(prompt.getIndex().toString() + " 1", reference3)
        ));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectMultiple_playsAllAudioInOrder() throws Exception {
        String reference1 = createMockReference(referenceManager, "file://audio.mp3");
        String reference2 = createMockReference(referenceManager, "file://audio1.mp3");
        String reference3 = createMockReference(referenceManager, "file://audio2.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(
                new Clip(prompt.getIndex().toString(), reference1),
                new Clip(prompt.getIndex().toString() + " 0", reference2),
                new Clip(prompt.getIndex().toString() + " 1", reference3)
        ));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelect_logsAutoplayAudioChoiceEvent() throws Exception {
        createMockReference(referenceManager, "file://audio.mp3");
        createMockReference(referenceManager, "file://audio1.mp3");
        createMockReference(referenceManager, "file://audio2.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_MULTI)
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

        autoplayer.autoplayIfNeeded(prompt);
        verify(analytics).logEvent("Prompt", "AutoplayAudioChoice", FORM_IDENTIFIER_HASH);
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_andIsSelectOne_playsAllSelectAudioInOrder() throws Exception {
        String reference1 = createMockReference(referenceManager, "file://audio1.mp3");
        String reference2 = createMockReference(referenceManager, "file://audio2.mp3");

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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(
                new Clip(prompt.getIndex().toString() + " 0", reference1),
                new Clip(prompt.getIndex().toString() + " 1", reference2)
        ));
    }

    @Test
    public void whenPromptHasAutoplayAudio_butNoAudioURI_andIsSelectOne_doesNotLogAutoplayAudioLabelEvent() throws Exception {
        createMockReference(referenceManager, "file://audio1.mp3");
        createMockReference(referenceManager, "file://audio2.mp3");

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

        autoplayer.autoplayIfNeeded(prompt);
        verify(analytics, never()).logEvent("Prompt", "AutoplayAudioLabel", FORM_IDENTIFIER_HASH);
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_butNoSelectChoiceAudio_playsPromptAudio() throws Exception {
        String reference = createMockReference(referenceManager, "file://audio.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelect_butNoSelectChoiceAudio_doesNotLogAutoplayAudioChoiceEvent() throws Exception {
        createMockReference(referenceManager, "file://audio.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAudioURI("file://audio.mp3")
                .withAdditionalAttribute("autoplay", "audio")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .build();

        autoplayer.autoplayIfNeeded(prompt);
        verify(analytics, never()).logEvent("Prompt", "AutoplayAudioChoice", FORM_IDENTIFIER_HASH);
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_withMinimalAppearance_playsPromptAudio() throws Exception {
        createMockReference(referenceManager, "file://audio1.mp3");
        createMockReference(referenceManager, "file://audio2.mp3");
        String reference = createMockReference(referenceManager, "file://audio.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance(MINIMAL)
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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_withNoButtonsAppearance_playsPromptAudio() throws Exception {
        createMockReference(referenceManager, "file://audio1.mp3");
        createMockReference(referenceManager, "file://audio2.mp3");
        String reference = createMockReference(referenceManager, "file://audio.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance("whatever " + NO_BUTTONS)
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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test
    public void whenPromptHasAutoplayAudio_andIsSelectOne_withDeprecatedCompactAppearance_playsPromptAudio() throws Exception {
        createMockReference(referenceManager, "file://audio1.mp3");
        createMockReference(referenceManager, "file://audio2.mp3");
        String reference = createMockReference(referenceManager, "file://audio.mp3");

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_SELECT_ONE)
                .withAppearance(COMPACT)
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

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(true));
        verify(audioHelper).playInOrder(asList(new Clip(prompt.getIndex().toString(), reference)));
    }

    @Test // We only support audio autoplaying with the helper right now
    public void whenPromptHasAutoplayVideo_returnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAdditionalAttribute("autoplay", "video")
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(false));
        verify(audioHelper, never()).playInOrder(any());
    }

    @Test
    public void whenPromptHasNoAutoplay_returnsFalse() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAdditionalAttribute("autoplay", null)
                .build();

        assertThat(autoplayer.autoplayIfNeeded(prompt), equalTo(false));
        verify(audioHelper, never()).playInOrder(any());
    }

    @Test
    public void whenPromptHasNoAutoplay_doesNotLogEvents() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAdditionalAttribute("autoplay", null)
                .build();

        autoplayer.autoplayIfNeeded(prompt);
        verify(analytics, never()).logEvent(any(), any());
        verify(analytics, never()).logEvent(any(), any(), any());
    }
}