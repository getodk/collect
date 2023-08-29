package org.odk.collect.android.feature.formentry;

import static org.odk.collect.android.utilities.FileUtils.copyFileFromResources;

import android.app.Application;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.OkDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingTest {

    private StubAudioRecorder stubAudioRecorderViewModel;

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public AudioRecorder providesAudioRecorder(Application application) {
            if (stubAudioRecorderViewModel == null) {
                try {
                    File stubRecording = File.createTempFile("test", ".m4a");
                    stubRecording.deleteOnExit();

                    copyFileFromResources("media/test.m4a", stubRecording.getAbsolutePath());
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
            .around(rule);

    @Test
    public void onAudioQuestion_withoutAudioQuality_canRecordInApp() {
        new MainMenuPage()
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(org.odk.collect.strings.R.string.capture_audio)
                .clickOnContentDescription(org.odk.collect.strings.R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(org.odk.collect.strings.R.string.stop_recording)
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.capture_audio)
                .assertContentDescriptionDisplayed(org.odk.collect.strings.R.string.play_audio);
    }

    @Test
    public void onAudioQuestion_withQualitySpecified_canRecordAudioInApp() {
        rule.startAtMainMenu()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .assertContentDescriptionNotDisplayed(org.odk.collect.strings.R.string.stop_recording)
                .clickOnString(org.odk.collect.strings.R.string.capture_audio)
                .clickOnContentDescription(org.odk.collect.strings.R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(org.odk.collect.strings.R.string.stop_recording)
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.capture_audio)
                .assertContentDescriptionDisplayed(org.odk.collect.strings.R.string.play_audio);
    }

    @Test
    public void whileRecording_pressingBack_showsWarning_andStaysOnSameScreen() {
        rule.startAtMainMenu()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(org.odk.collect.strings.R.string.capture_audio)
                .pressBack(new OkDialog())
                .clickOK(new FormEntryPage("Audio Question"))
                .assertQuestion("What does it sound like?");
    }

    @Test
    public void whileRecording_swipingToADifferentScreen_showsWarning_andStaysOnSameScreen() {
        rule.startAtMainMenu()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(org.odk.collect.strings.R.string.capture_audio)
                .swipeToEndScreenWhileRecording()
                .clickOK(new FormEntryPage("Audio Question"))
                .assertQuestion("What does it sound like?");
    }
}
