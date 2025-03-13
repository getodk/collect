package org.odk.collect.android.feature.projects

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.mobiledevicemanagement.SETTINGS_JSON_KEY

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
                sendBroadcast(
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
            .assertOnPage(MainMenuPage())
            .openProjectSettingsDialog()
            .assertCurrentProject("project1", "john / john.com")
    }

    @Test
    fun whenNewProjectIsCreatedViaMDMWhileOutsideTheLandingScreen_navigateToMainMenuNextTimeTheLandingScreenOpens() {
        sendBroadcast(
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
                sendBroadcast(
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
                sendBroadcast(
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
                sendBroadcast(
                    """
                    {
                        "general": {},
                        "admin": {
                            "edit_saved": false
                        }
                    }
                    """.trimIndent()
                )
            }
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .openMainMenuSettings()
            .assertDraftsUnchecked()
    }

    @Test
    fun whenCurrentProjectIsUpdatedViaMDMWhileOutsideMainMenu_updateApplySettingsNextTimeMainMenuOpens() {
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml")

        rule.startAtFirstLaunch()
            .clickTryCollect()
            .setServer(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .also {
                sendBroadcast(
                    """
                    {
                        "general": {
                            "server_url": "${testDependencies.server.url}"
                        },
                        "admin": {
                            "jump_to": false
                        }
                    }
                    """.trimIndent()
                )
            }
            .clickOnForm("One Question")
            .assertGoToIconExists()
            .pressBackAndDiscardForm()
            .clickFillBlankForm()
            .clickOnForm("One Question")
            .assertGoToIconDoesNotExist()
    }

    @Test
    fun whenInactiveProjectIsUpdatedViaMDMWW_settingsAreUpdatedAfterSwitchingToThatProject() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("http://john.com")
            .inputUsername("john")
            .addProject()
            .openProjectSettingsDialog()
            .selectProject("Demo project")
            .also {
                sendBroadcast(
                    """
                    {
                        "general": {
                            "server_url": "http://john.com",
                            "username": "john"
                        },
                        "admin": {
                            "edit_saved": false
                        }
                    }
                    """.trimIndent()
                )
            }
            .openProjectSettingsDialog()
            .selectProject("john.com")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .openMainMenuSettings()
            .assertDraftsUnchecked()
    }

    private fun sendBroadcast(settings: String) {
        val bundle = Bundle().apply {
            putString(SETTINGS_JSON_KEY, settings)
        }
        whenever(testDependencies.restrictionsManager.applicationRestrictions).thenReturn(bundle)

        testDependencies.broadcastReceiverRegister.broadcast(
            ApplicationProvider.getApplicationContext(),
            Intent(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        )
    }
}
