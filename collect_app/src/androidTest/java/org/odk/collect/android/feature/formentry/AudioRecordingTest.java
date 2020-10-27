package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

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
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.utilities.ActivityAvailability;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingTest {

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public ActivityAvailability providesActivityAvailability(Context context) {
            return new ActivityAvailability(context) {
                @Override
                public boolean isActivityAvailable(Intent intent) {
                    return true;
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
                .clickOnString(R.string.capture_audio)
                .clickOnString(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }

    @Test
    public void answeringExternalString_whileRecording_works() {
        Intent data = new Intent();
        data.putExtra("value", "external string");
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);
        intending(hasAction("com.example.EXTERNAL")).respondWith(activityResult);

        new MainMenuPage(rule).assertOnPage()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickExperimental()
                .clickExternalAppRecording()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .copyForm("audio-question-with-external.xml")
                .startBlankForm("Audio Question with External")
                .clickOnString(R.string.capture_audio)
                .clickOnString(R.string.launch_app)
                .assertText("external string")
                .clickOnString(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }
}
