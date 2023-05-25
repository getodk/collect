package org.odk.collect.android.feature.formentry

import android.content.Context
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.testshared.FakeNetworkStateProvider

class FormSavedSnackbarTest {
    private val rule = CollectTestRule()

    private val networkStateProvider = FakeNetworkStateProvider()

    private val testDependencies = object : TestDependencies() {
        override fun providesNetworkStateProvider(context: Context?): NetworkStateProvider {
            return networkStateProvider
        }
    }

    @get:Rule
    val copyFormChain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun whenFormSavedAsDraft_displaySnackbarWithEditAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(R.string.form_saved_as_draft)
            .clickOnString(R.string.edit_form)
            .assertText("25")
            .assertText(R.string.jump_to_beginning)
            .assertText(R.string.jump_to_end)
    }

    @Test
    fun whenFormFinalized_displaySnackbarWithViewAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickFinalize()
            .assertText(R.string.form_saved)
            .clickOnString(R.string.view_form)
            .assertText("25")
            .assertTextDoesNotExist(R.string.jump_to_beginning)
            .assertTextDoesNotExist(R.string.jump_to_end)
            .assertText(R.string.exit)
    }

    @Test
    fun whenFormFinalizedWithAutoSend_displaySnackbarWithViewAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickAutoSend()
            .clickOnString(R.string.wifi_cellular_autosend)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSend()
            .assertText(R.string.form_sending)
            .clickOnString(R.string.view_form)
            .assertText("25")
            .assertTextDoesNotExist(R.string.jump_to_beginning)
            .assertTextDoesNotExist(R.string.jump_to_end)
            .assertText(R.string.exit)
    }

    @Test
    fun whenFormFinalizedWithAutoSendButNoInternetConnection_displaySnackbarWithViewAction() {
        networkStateProvider.disableInternetConnection()

        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickAutoSend()
            .clickOnString(R.string.wifi_cellular_autosend)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSend()
            .assertText(R.string.form_sending_failed)
            .clickOnString(R.string.view_form)
            .assertText("25")
            .assertTextDoesNotExist(R.string.jump_to_beginning)
            .assertTextDoesNotExist(R.string.jump_to_end)
            .assertText(R.string.exit)
    }

    @Test
    fun snackbarCanBeDismissed_andWillNotBeDisplayedAgainAfterRecreatingTheActivity() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(R.string.form_saved_as_draft)
            .closeSnackbar()
            .assertTextDoesNotExist(R.string.form_saved_as_draft)
            .rotateToLandscape(MainMenuPage())
            .assertTextDoesNotExist(R.string.form_saved_as_draft)
    }
}
