package org.odk.collect.android.widgets.utilities;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

public class RecordingRequesterProviderTest {

    private final RecordingRequesterProvider provider = new RecordingRequesterProvider(
            mock(InternalRecordingRequester.class),
            mock(ExternalAppRecordingRequester.class)
    );

    @Test
    public void whenNoQualitySpecified_andSettingExternalNotPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, false);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenNoQualitySpecified_andSettingExternalPreferred_createsExternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, true);
        assertThat(recordingRequester, instanceOf(ExternalAppRecordingRequester.class));
    }

    @Test
    public void whenQualityIsNormal_andSettingExternalNotPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "normal")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, false);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsNormal_andSettingExternalPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "normal")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, true);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsVoiceOnly_andSettingExternalNotPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "voice-only")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, false);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsVoiceOnly_andSettingExternalPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "voice-only")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, true);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsLow_andSettingExternalNotPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "low")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, false);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsLow_andSettingExternalPreferred_createsInternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "low")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, true);
        assertThat(recordingRequester, instanceOf(InternalRecordingRequester.class));
    }

    @Test
    public void whenQualityIsExternal_andSettingExternalPreferred_createsExternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "external")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, true);
        assertThat(recordingRequester, instanceOf(ExternalAppRecordingRequester.class));
    }

    @Test
    public void whenQualityIsExternal_andSettingExternalNotPreferred_createsExternalRecordingRequester() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "external")
                .build();

        RecordingRequester recordingRequester = provider.create(prompt, false);
        assertThat(recordingRequester, instanceOf(ExternalAppRecordingRequester.class));
    }
}
