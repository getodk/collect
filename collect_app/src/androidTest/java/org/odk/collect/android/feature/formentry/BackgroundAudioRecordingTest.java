package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Application;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioRecordingTest {

    private StubAudioRecorder stubAudioRecorderViewModel;

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public AudioRecorder providesAudioRecorder(Application application) {
            if (stubAudioRecorderViewModel == null) {
                try {
                    File stubRecording = File.createTempFile("test", ".m4a");
                    stubRecording.deleteOnExit();

                    copyFileFromAssets("media/test.m4a", stubRecording.getAbsolutePath());
                    stubAudioRecorderViewModel = new StubAudioRecorder(stubRecording.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return stubAudioRecorderViewModel;
        }
    };

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule);

    @Test
    public void whenBackgroundAudioRecordingEnabled_fillingOutForm_recordsAudio() {
        FormEntryPage formEntryPage = rule.mainMenu()
                .enableBackgroundAudioRecording()
                .copyForm("one-question.xml")
                .startBlankForm("One Question");
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        FormEndPage formEndPage = formEntryPage
                .inputText("123")
                .swipeToEndScreen();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        formEndPage.clickSaveAndExit();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));

        assertThat(stubAudioRecorderViewModel.getLastRecording(), notNullValue());
        assertThat(stubAudioRecorderViewModel.getLastRecording().exists(), is(true));
    }

    /**
     * This could probably be tested at a lower level when the background recording implementation
     * stabilizes.
     */
    @Test
    public void whenBackgroundAudioRecordingEnabled_fillingOutForm_doesntShowStopOrPauseButtons() {
        rule.mainMenu()
                .enableBackgroundAudioRecording()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(R.string.pause_recording);
    }

    @Test
    public void whenBackgroundAudioRecordingEnabled_uncheckingRecordAudio_endsAndDeletesRecording_andDisablesItForNextFormFill() {
        FormEntryPage formEntryPage = rule.mainMenu()
                .enableBackgroundAudioRecording()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .clickOptionsIcon()
                .clickRecordAudio()
                .clickOk();

        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));
        assertThat(stubAudioRecorderViewModel.getLastRecording(), is(nullValue()));

        formEntryPage.closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage(rule), rule))
                .clickIgnoreChanges()
                .startBlankForm("One Question");

        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));
    }
}
