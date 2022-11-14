package org.odk.collect.android.audio

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule

@RunWith(AndroidJUnit4::class)
class BackgroundAudioHelpDialogFragmentTest {
    @get:Rule
    var launcherRule = FragmentScenarioLauncherRule(
        R.style.Theme_MaterialComponents
    )

    @Test
    fun `has ok button`() {
        val scenario = launcherRule.launch(
            BackgroundAudioHelpDialogFragment::class.java
        )

        scenario.onFragment { f: BackgroundAudioHelpDialogFragment ->
            val dialog = f.dialog as AlertDialog?
            val okButton = dialog!!.getButton(DialogInterface.BUTTON_POSITIVE)

            assertTrue(okButton.isShown)
        }
    }
}
