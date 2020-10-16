package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.audiorecorder.recording.AudioRecorderActivity;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity.IntentForResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class InternalRecordingRequesterTest {

    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();

    private Activity activity;
    private InternalRecordingRequester requester;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).get();
        requester = new InternalRecordingRequester(activity, waitingForDataRegistry);
    }

    @Test
    public void requestRecording_startsRecordSoundIntentAndSetsWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        Intent startedActivity = shadowOf(activity).getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(), is(AudioRecorderActivity.class.getName()));

        IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(RequestCodes.INTERNAL_AUDIO_CAPTURE));
    }
}