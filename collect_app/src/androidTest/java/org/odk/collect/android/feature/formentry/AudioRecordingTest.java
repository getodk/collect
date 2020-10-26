package org.odk.collect.android.feature.formentry;

import android.Manifest;

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
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingTest {

    public final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule);

    @Test
    public void onAudioQuestion_canRecordAudio() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickExperimental()
                .clickExternalAppRecording()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
//                .assertTextNotDisplayed(R.string.stop_recording)
                .clickOnString(R.string.capture_audio)
                .clickOnString(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
//                .assertTextNotDisplayed(R.string.stop_recording)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }
}
