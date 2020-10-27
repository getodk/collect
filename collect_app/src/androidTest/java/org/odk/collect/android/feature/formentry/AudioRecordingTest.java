package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingTest {

    private final FakeAudioRecorderViewModel fakeAudioRecorderViewModel = new FakeAudioRecorderViewModel();

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public AudioRecorderViewModelFactory providesAudioRecorderViewModelFactory(Application application) {
            return new AudioRecorderViewModelFactory(application) {
                @Override
                public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) fakeAudioRecorderViewModel;
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
                .clickOnMenu()
                .clickGeneralSettings()
                .clickExperimental()
                .clickExternalAppRecording()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

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
                .clickOnMenu()
                .clickGeneralSettings()
                .clickExperimental()
                .clickExternalAppRecording()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .swipeToEndScreen();

        assertThat(fakeAudioRecorderViewModel.wasCancelled, is(true));

        page.swipeToPreviousQuestion("What does it sound like?")
                .assertEnabled(R.string.capture_audio);
    }

    private static class FakeAudioRecorderViewModel extends AudioRecorderViewModel {

        private final MutableLiveData<Boolean> isRecording = new MutableLiveData<>(false);
        private final MutableLiveData<File> file = new MutableLiveData<>(null);
        private boolean wasCancelled;

        @NotNull
        @Override
        public LiveData<Boolean> isRecording() {
            return isRecording;
        }

        @NotNull
        @Override
        public LiveData<File> getRecording(@NotNull String sessionId) {
            return file;
        }

        @Override
        public void start(@NotNull String sessionId) {
            wasCancelled = false;
            isRecording.setValue(true);
        }

        @Override
        public void stop() {
            this.isRecording.setValue(false);

            try {
                File tempFile = File.createTempFile("temp", ".m4a");
                tempFile.deleteOnExit();
                copyFileFromAssets("media/test.m4a", tempFile.getAbsolutePath());

                this.file.setValue(tempFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void cancel() {
            wasCancelled = true;
            isRecording.setValue(false);
        }

        @Override
        public void endSession() {
            this.file.setValue(null);
        }
    }
}
