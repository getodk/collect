package org.odk.collect.android.audio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.content.DialogInterface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioHelpDialogFragmentTest {

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Test
    public void hasOkButton() {
        FragmentScenario<BackgroundAudioHelpDialogFragment> scenario = launcherRule.launchDialogFragment(BackgroundAudioHelpDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();
            Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(okButton.isShown(), is(true));
        });
    }
}
