package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
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
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.OkDialog;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorderViewModel;

import java.io.File;
import java.io.IOException;

import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingTest {

    private StubAudioRecorderViewModel stubAudioRecorderViewModel;

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public AudioRecorderViewModelFactory providesAudioRecorderViewModelFactory(Application application) {
            return new AudioRecorderViewModelFactory(application) {
                @Override
                public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                    if (stubAudioRecorderViewModel == null) {
                        try {
                            File stubRecording = File.createTempFile("test", ".m4a");
                            stubRecording.deleteOnExit();

                            copyFileFromAssets("media/test.m4a", stubRecording.getAbsolutePath());
                            stubAudioRecorderViewModel = new StubAudioRecorderViewModel(stubRecording.getAbsolutePath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return (T) stubAudioRecorderViewModel;
                }
            };
        }
    };

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule);

    @Test
    public void onAudioQuestion_withQualitySpecified_canRecordAudioInApp() {
        new MainMenuPage(rule).assertOnPage()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .clickOnString(R.string.capture_audio)
                .clickOnContentDescription(R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }

    @Test
    public void whileRecording_swipingToADifferentScreen_showsWarning_andStaysOnSameScreen() {
        new MainMenuPage(rule).assertOnPage()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .swipeToEndScreenWhileRecording()
                .clickOK(new FormEntryPage("Audio Question", rule))

                .assertQuestion("What does it sound like?")
                .clickOnContentDescription(R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }

    @Test
    public void whileRecording_quittingForm_showsWarning_andStaysOnSameScreen() {
        new MainMenuPage(rule).assertOnPage()
                .copyForm("internal-audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .pressBack(new OkDialog(rule))
                .clickOK(new FormEntryPage("Audio Question", rule))

                .assertQuestion("What does it sound like?")
                .clickOnContentDescription(R.string.stop_recording)
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }
}
