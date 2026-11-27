package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.GetBlankFormPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class GetBlankFormsTest {
    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(false)

    @get:Rule
    val copyFormChain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun whenFormHasVeryLongTitle_canBeDownloadedAndFilled() {
        val title = "ეს არის ტესტური ფორმა ძალიან გრძელი სათაურით, რომელიც ამოწმებს, რომ აპლიკაცია სწორად ჩამოტვირთავს და ხსნის ასეთ ფორმებს"
        testDependencies.server.addForm(title, "one_question_long_title", "1", "one-question-long-title.xml")

        rule.withProject(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
            .startBlankForm(title)
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertNumberOfEditableForms(1)
    }

    @Test
    fun whenThereIsAnAuthenticationErrorFetchingFormList_allowsUserToReenterCredentials() {
        testDependencies.server.setCredentials("Draymond", "Green")
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml")

        rule.withProject(testDependencies.server.url)
            .clickGetBlankFormWithAuthenticationError()
            .fillUsername("Draymond")
            .fillPassword("Green")
            .clickOK(GetBlankFormPage())
            .assertText("One Question")
    }

    @Test
    fun whenThereIsAnErrorFetchingFormList_showsError() {
        testDependencies.server.alwaysReturnError()

        rule.withProject(testDependencies.server.url)
            .clickGetBlankFormWithError()
            .assertText(org.odk.collect.strings.R.string.load_remote_form_error)
            .clickOK(GetBlankFormPage())
    }

    @Test
    fun whenThereIsAnErrorFetchingForms_showsError() {
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml")
        testDependencies.server.errorOnFetchingForms()

        rule.withProject(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .assertMessage("1 of 1 downloads failed!")
            .showDetails()
            .assertError("The server https://server.example.com returned status code 500. If you keep having this problem, report it to the person who asked you to collect data.")
            .navigateBack()
    }
}
