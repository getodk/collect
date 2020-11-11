package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.FormHierarchyPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorderViewModel;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    public final IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule);

    @Test
    public void onAudioQuestion_canRecordAudio() {
        new MainMenuPage(rule).assertOnPage()
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .assertTextNotDisplayed(R.string.stop_recording)
                .clickOnString(R.string.capture_audio)
                .clickOnString(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }

    @Test
    public void whileRecording_swipingToADifferentScreen_cancelsRecording() {
        final FormEndPage page = new MainMenuPage(rule).assertOnPage()
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .swipeToEndScreen()
                .assertTextNotDisplayed(R.string.stop_recording);

        assertThat(stubAudioRecorderViewModel.getWasCleanedUp(), is(true));

        page.swipeToPreviousQuestion("What does it sound like?")
                .assertEnabled(R.string.capture_audio);
    }

    @Test
    public void whileRecording_openingHierarchyMenu_cancelsRecording() {
        final FormHierarchyPage page = new MainMenuPage(rule).assertOnPage()
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .clickGoToArrow();

        assertThat(stubAudioRecorderViewModel.getWasCleanedUp(), is(true));

        page.pressBack(new FormEntryPage("Audio Question", rule))
                .assertTextNotDisplayed(R.string.stop_recording)
                .assertEnabled(R.string.capture_audio);
    }

    @Test
    public void whileRecording_quittingForm_cancelsRecording() {
        final MainMenuPage page = new MainMenuPage(rule).assertOnPage()
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .pressBack(new SaveOrIgnoreDialog<>("Audio Question", new MainMenuPage(rule), rule))
                .clickSaveChanges();

        assertThat(stubAudioRecorderViewModel.getWasCleanedUp(), is(true));

        page.startBlankForm("Audio Question")
                .assertEnabled(R.string.capture_audio);
    }
}
