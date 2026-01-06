package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class EntityFormSpecVersionTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun fillingEntityRegistrationForm_whenFormUsesOldSpecVersion_doesNotCreateEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration-v2023.1.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Logan Roy")
    }

    @Test
    fun manualEntityFormDownload_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        testDependencies.server.addForm("one-question-entity-registration-v2020.1.xml")

        rule.withProject(testDependencies.server)
            .clickGetBlankForm()
            .clickClearAll()
            .clickForm("One Question Entity Registration")
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun automaticEntityFormDownload_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        testDependencies.server.addForm("one-question-entity-registration-v2020.1.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun syncEntityFormFromDisc_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration-v2020.1.xml")
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }
}
