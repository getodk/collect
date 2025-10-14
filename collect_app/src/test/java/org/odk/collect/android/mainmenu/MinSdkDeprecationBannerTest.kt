package org.odk.collect.android.mainmenu

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MinSdkDeprecationBannerTest {
    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    @Config(sdk = [26])
    fun `Banner is not displayed on API 26 and above`() {
        val scenario = launcherRule.launch(MinSdkDeprecationBanner::class.java)
        scenario.onFragment { fragment ->
            assertThat(fragment.requireView().visibility, equalTo(View.GONE))
        }
    }

    @Test
    @Config(sdk = [25])
    fun `Banner is displayed on API below 26 and disappears after clicking the dismiss button`() {
        val scenario = launcherRule.launch(MinSdkDeprecationBanner::class.java)
        scenario.onFragment { fragment ->
            assertThat(fragment.requireView().visibility, equalTo(View.VISIBLE))
            fragment.requireView().findViewById<MaterialButton>(R.id.dismiss_button).performClick()
            assertThat(fragment.requireView().visibility, equalTo(View.GONE))
        }
    }
}
