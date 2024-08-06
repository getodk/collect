package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.DeleteSavedFormPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class DeleteSavedFormTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun deletingAForm_removesFormFromFinalizedForms() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .swipeToEndScreen()
            .clickFinalize()

            .clickDeleteSavedForm()
            .clickForm("One Question")
            .clickDeleteSelected(1)
            .clickDeleteForms()
            .checkIsSnackbarWithMessageDisplayed(string.file_deleted_ok, 1)
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun whenFormHasCreatedEntity_doesNotAppearInListToDelete() {
        testDependencies.server.addForm("one-question-entity-registration.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .clickDeleteSavedForm()
            .assertTextDoesNotExist("One Question Entity Registration")
    }

    @Test
    fun accessingSortMenuInDeleteSavedInstancesShouldNotCrashTheAppAfterRotatingTheDevice() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .swipeToEndScreen()
            .clickFinalize()
            .clickDeleteSavedForm()
            .rotateToLandscape(DeleteSavedFormPage())
            .clickOnId(R.id.menu_sort)
            .assertText(string.sort_by)
    }
}
