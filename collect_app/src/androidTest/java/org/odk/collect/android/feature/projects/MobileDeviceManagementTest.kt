package org.odk.collect.android.feature.projects

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.mobiledevicemanagement.MDMConfigHandler.Companion.SETTINGS_JSON_KEY

@RunWith(AndroidJUnit4::class)
class MobileDeviceManagementTest {
    private val recentAppsRule = RecentAppsRule()
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(recentAppsRule)
        .around(rule)

    @Test
    fun whenNewProjectIsCreatedViaMDMWhileOnTheLandingScreen_navigateToMainMenu() {
        rule.startAtFirstLaunch()
            .also {
                saveConfig(
                    """
                    {
                        "general": {
                            "server_url": "http://john.com",
                            "username": "john"
                        },
                        "admin": {},
                        "project": {
                            "name": "project1"
                        }
                    }
                    """.trimIndent()
                )
                triggerBroadcastReceiver()
            }
            .assertOnPage(MainMenuPage())
            .openProjectSettingsDialog()
            .assertCurrentProject("project1", "john / john.com")
    }

    @Test
    fun whenNewProjectIsCreatedViaMDMWhileOutsideTheLandingScreen_navigateToMainMenuNextTimeTheLandingScreenOpens() {
        saveConfig(
            """
            {
                "general": {
                    "server_url": "http://john.com",
                    "username": "john"
                },
                "admin": {},
                "project": {
                    "name": "project1"
                }
            }
            """.trimIndent()
        )

        rule.relaunch(MainMenuPage())
    }

    @Test
    fun whenNewProjectIsCreatedViaMDMWhileOnTheMainMenu_createButDoNotSwitch() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .also {
                saveConfig(
                    """
                    {
                        "general": {
                            "server_url": "http://john.com",
                            "username": "john"
                        },
                        "admin": {},
                        "project": {
                            "name": "project1"
                        }
                    }
                    """.trimIndent()
                )
                triggerBroadcastReceiver()
            }
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("project1", "john / john.com")
    }

    @Test
    fun whenNewProjectIsCreatedViaMDMWhileOutsideMainMenu_createButDoNotSwitchNextTimeMainMenuOpens() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .clickFillBlankForm()
            .also {
                saveConfig(
                    """
                    {
                        "general": {
                            "server_url": "http://john.com",
                            "username": "john"
                        },
                        "admin": {},
                        "project": {
                            "name": "project1"
                        }
                    }
                    """.trimIndent()
                )
            }
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("project1", "john / john.com")
    }

    @Test
    fun whenCurrentProjectIsUpdatedViaMDMWWhileOnTheMainMenu_applySettingsImmediately() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .also {
                saveConfig(
                    """
                    {
                        "general": {},
                        "admin": {
                            "edit_saved": false,
                            "project": {
                                "name": "project1"
                            }
                        }
                    }
                    """.trimIndent()
                )
                triggerBroadcastReceiver()
            }

        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        val areDraftsEnabled = component.settingsProvider().getUnprotectedSettings().getBoolean("edit_saved")
        assertThat(areDraftsEnabled, equalTo(false))
    }

    @Test
    fun whenCurrentProjectIsUpdatedViaMDMWhileOutsideMainMenu_updateApplySettingsNextTimeMainMenuOpens() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .clickFillBlankForm()
            .also {
                saveConfig(
                    """
                    {
                        "general": {},
                        "admin": {
                            "edit_saved": false,
                            "project": {
                                "name": "project1"
                            }
                        }
                    }
                    """.trimIndent()
                )
            }
            .pressBack(MainMenuPage())

        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        val areDraftsEnabled = component.settingsProvider().getUnprotectedSettings().getBoolean("edit_saved")
        assertThat(areDraftsEnabled, equalTo(false))
    }

    private fun saveConfig(settings: String) {
        val bundle = Bundle().apply {
            putString(SETTINGS_JSON_KEY, settings)
        }
        whenever(testDependencies.restrictionsManager.applicationRestrictions).thenReturn(bundle)
    }

    private fun triggerBroadcastReceiver() {
        testDependencies.broadcastReceiverRegister.registeredReceivers.first().onReceive(
            ApplicationProvider.getApplicationContext(),
            Intent(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        )
    }
}
