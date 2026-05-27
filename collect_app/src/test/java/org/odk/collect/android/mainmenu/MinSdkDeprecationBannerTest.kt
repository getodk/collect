package org.odk.collect.android.mainmenu

import android.view.View
import androidx.core.net.toUri
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.MockWebPageService
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MinSdkDeprecationBannerTest {
    private val appState = AppState()
    private val webPageService = MockWebPageService()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(MinSdkDeprecationBanner::class) {
                MinSdkDeprecationBanner(appState, webPageService)
            }.build()
    )

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

    @Test
    @Config(sdk = [25])
    fun `Clicking Learn More opens forum thread`() {
        val scenario = launcherRule.launch(MinSdkDeprecationBanner::class.java)
        scenario.onFragment { fragment ->
            fragment.requireView().findViewById<MaterialButton>(R.id.learn_more_button).performClick()
            assertThat(
                webPageService.openedPages,
                equalTo(listOf("https://forum.getodk.org/t/56946".toUri()))
            )
        }
    }
}
