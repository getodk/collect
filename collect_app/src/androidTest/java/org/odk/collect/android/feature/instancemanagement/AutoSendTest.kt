package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.kxml2.kdom.Element
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.ErrorPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ViewSentFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.NotificationDrawerRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.async.Scheduler
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class AutoSendTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()
    private val notificationDrawerRule = NotificationDrawerRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(notificationDrawerRule)
        .around(rule)

    @Test
    fun whenAutoSendEnabled_fillingAndFinalizingForm_sendsFormAndNotifiesUser() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                R.string.wifi_cellular_autosend
            )
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question")

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Forms upload succeeded", "All uploads succeeded!")
            .clickNotification(
                "ODK Collect",
                "Forms upload succeeded",
                ViewSentFormPage()
            ).pressBack(MainMenuPage())
    }

    @Test
    fun whenAutoSendEnabled_fillingAndFinalizingForm_notifiesUserWhenSendingFails() {
        testDependencies.server.alwaysReturnError()

        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                R.string.wifi_cellular_autosend
            )
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.clickViewSentForm(1)
            .assertText("One Question")

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Forms upload failed", "1 of 1 uploads failed!")
            .clickAction(
                "ODK Collect",
                "Forms upload failed",
                "Show details",
                ErrorPage()
            )
    }

    @Test
    fun whenFormHasAutoSend_fillingAndFinalizingForm_sendsFormAndNotifiesUser_regardlessOfSetting() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                R.string.wifi_autosend
            )
            .copyForm("one-question-autosend.xml")
            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.networkStateProvider.goOnline(Scheduler.NetworkType.CELLULAR)
        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(1)
            .assertText("One Question Autosend")

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Forms upload succeeded", "All uploads succeeded!")
            .clickNotification(
                "ODK Collect",
                "Forms upload succeeded",
                ViewSentFormPage()
            ).pressBack(MainMenuPage())
    }

    @Test
    fun whenFormHasAutoSend_canAutoSendMultipleForms() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question-autosend.xml")

            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

            .startBlankForm("One Question Autosend")
            .inputText("32")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage
            .clickViewSentForm(2)
    }

    @Test
    fun whenFormHasAutoSend_fillingAndFinalizingForm_notifiesUserWhenSendingFails_regardlessOfSetting() {
        testDependencies.server.alwaysReturnError()

        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                R.string.wifi_autosend
            )
            .copyForm("one-question-autosend.xml")
            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.networkStateProvider.goOnline(Scheduler.NetworkType.CELLULAR)
        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.clickViewSentForm(1)
            .assertText("One Question Autosend")

        notificationDrawerRule
            .open()
            .assertNotification("ODK Collect", "Forms upload failed", "1 of 1 uploads failed!")
            .clickAction(
                "ODK Collect",
                "Forms upload failed",
                "Show details",
                ErrorPage()
            )
    }

    @Test
    fun whenFormHasAutoSendDisabled_fillingAndFinalizingForm_doesNotSendForm_regardlessOfSetting() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                R.string.wifi_cellular_autosend
            )
            .copyForm("one-question-autosend-disabled.xml")
            .startBlankForm("One Question Autosend Disabled")
            .inputText("31")
            .swipeToEndScreen()
            .clickFinalize()

        testDependencies.scheduler.runDeferredTasks()
        mainMenuPage.assertNumberOfFinalizedForms(1)
    }

    @Test
    fun whenAutoSendDisabled_fillingAndFinalizingForm_doesNotSendFormAutomatically() {
        val mainMenuPage = rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("what is your age", "31"))

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.assertNumberOfFinalizedForms(1)
    }

    @Test
    fun whenFormHasAutoSend_formsAreSentInOldestFirstOrder() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question-autosend.xml")

            .startBlankForm("One Question Autosend")
            .inputText("31")
            .swipeToEndScreen()
            .clickSend()

            .startBlankForm("One Question Autosend")
            .inputText("32")
            .swipeToEndScreen()
            .clickSend()

        testDependencies.scheduler.runDeferredTasks()

        val firstFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[0].inputStream().reader()).rootElement
        val secondFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[1].inputStream().reader()).rootElement

        assertThat((firstFormRootElement.getChild(0) as Element).getChild(0), equalTo("31"))
        assertThat((secondFormRootElement.getChild(0) as Element).getChild(0), equalTo("32"))
    }
}
