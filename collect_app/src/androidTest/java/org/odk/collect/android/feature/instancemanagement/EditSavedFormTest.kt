package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SendFinalizedFormPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class EditSavedFormTest {
    private val rule = CollectTestRule()

    val testDependencies: TestDependencies = TestDependencies()
    private val recentAppsRule = RecentAppsRule()

    @get:Rule
    var copyFormChain: RuleChain = chain(testDependencies)
        .around(recentAppsRule)
        .around(rule)

    @Test
    fun finalizedFormIsNotAvailableForEditsInTheListOfDrafts() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .assertNumberOfEditableForms(0)
            .clickDrafts()
            .assertTextDoesNotExist("One Question")

            // Tests that search doesn't change visibility. Move down to lower testing level.
            // (possibly when replacing CursorLoader)
            .clickMenuFilter()
            .searchInBar("One Question".substring(0, 1))
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun sentFormIsNotAvailableForEditsInTheListOfDrafts() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(0)
            .clickDrafts()
            .assertTextDoesNotExist("One Question")

            // Tests that search doesn't change visibility. Move down to lower testing level.
            // (possibly when replacing CursorLoader)
            .clickMenuFilter()
            .searchInBar("One Question".substring(0, 1))
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun failedToSendFormIsNotAvailableForEditsInTheListOfDrafts() {
        testDependencies.server.alwaysReturnError()

        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(0)
            .clickDrafts()
            .assertTextDoesNotExist("One Question")

            // Tests that search doesn't change visibility. Move down to lower testing level
            // (possibly when replacing CursorLoader)
            .clickMenuFilter()
            .searchInBar("One Question".substring(0, 1))
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun editingAFinalizedForm_opensANewFormWithTheSameAnswers() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .assertText("123")
    }

    @Test
    fun editingAFinalizedForm_createsANewFormAndKeepsTheOriginalOneIntact() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .answerQuestion("what is your age", "456")
            .pressBackAndSaveAsDraft(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickDrafts(1)
            .clickOnForm("One Question")
            .assertText("456")
            .clickGoToEnd()
            .clickSaveAsDraft()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .assertText("123")
    }

    @Test
    fun editingASentForm_createsANewFormAndKeepsTheOriginalOneIntact() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickViewSentForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .answerQuestion("what is your age", "456")
            .pressBackAndSaveAsDraft()

            .clickDrafts(1)
            .clickOnForm("One Question")
            .assertText("456")
            .clickGoToEnd()
            .clickSaveAsDraft()

            .clickViewSentForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .assertText("123")
    }

    @Test
    fun discardingChangesWhenEditingFinalizedForm_createsDraftWithOriginalAnswers() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .answerQuestion("what is your age", "456")
            .pressBackAndDiscardChanges(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickDrafts(1)
            .clickOnForm("One Question")
            .assertText("123")
    }

    @Test
    fun killingAppWhenEditingFinalizedForm_createsSavepointForFormRecovery() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .answerQuestion("what is your age", "456")
            .killAndReopenApp(rule, recentAppsRule, MainMenuPage())

            .clickDrafts(1)
            .clickOnFormWithSavepoint("One Question")
            .clickRecover(FormHierarchyPage("One Question"))
            .assertText("456")
    }

    @Test
    fun editingAnEditedForm_updatesInstanceIdAndDeprecatedId() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .pressBackAndSaveAsDraft(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickDrafts(1)
            .clickOnForm("One Question")
            .clickGoToEnd()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .swipeToEndScreen()
            .clickFinalize(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickSendFinalizedForm(2)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

        val firstEditedFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[1].inputStream().reader()).rootElement
        val firstEditedFormMetaElement = firstEditedFormRootElement.getElement(null, "meta")
        val firstEditedFormInstanceID = firstEditedFormMetaElement.getElement(null, "instanceID").getText(0)

        val secondEditedFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[2].inputStream().reader()).rootElement
        val secondEditedFormMetaElement = secondEditedFormRootElement.getElement(null, "meta")
        val secondEditedFormInstanceID = secondEditedFormMetaElement.getElement(null, "instanceID").getText(0)
        val secondEditedFormDeprecatedID = secondEditedFormMetaElement.getElement(null, "deprecatedID").getText(0)

        assertThat(firstEditedFormInstanceID, equalTo(secondEditedFormDeprecatedID))
        assertThat(firstEditedFormInstanceID, not(secondEditedFormInstanceID))
    }

    @Test
    fun savingEditedFormMultipleTimes_preservesDeprecatedId() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "123")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(MainMenuPage())

            .clickViewSentForm(1)
            .clickOnForm("One Question")
            .editForm("One Question")
            .clickOnQuestion("what is your age")
            .answerQuestion("what is your age", "456")
            .pressBackAndSaveAsDraft()

            .clickDrafts(1)
            .clickOnForm("One Question")
            .assertText("456")
            .clickGoToEnd()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())

        val firstFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[0].inputStream().reader()).rootElement
        val firstFormMetaElement = firstFormRootElement.getElement(null, "meta")
        val firstFormInstanceID = firstFormMetaElement.getElement(null, "instanceID").getText(0)

        val secondFormRootElement = XFormParser.getXMLDocument(testDependencies.server.submissions[1].inputStream().reader()).rootElement
        val secondFormMetaElement = secondFormRootElement.getElement(null, "meta")
        val secondFormDeprecatedID = secondFormMetaElement.getElement(null, "deprecatedID").getText(0)

        assertThat(firstFormInstanceID, equalTo(secondFormDeprecatedID))
    }
}
