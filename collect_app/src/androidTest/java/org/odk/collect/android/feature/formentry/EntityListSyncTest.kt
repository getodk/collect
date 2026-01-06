package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
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

@RunWith(AndroidJUnit4::class)
class EntityListSyncTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun entityListSecondaryInstancesAreConsistentBetweenFollowUpForms() {
        testDependencies.server.apply {
            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "updated-people.csv", 2))
            )
        }

        rule.withProject(testDependencies.server)
            .clickGetBlankForm()
            .clickClearAll()
            .clickForm("One Question Entity Update")
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .startBlankForm("One Question Entity Update")
            .assertText("Roman Roy")
            .pressBackAndDiscardForm()

            .clickGetBlankForm()
            .clickGetSelected() // Collect automatically only selects the un-downloaded forms
            .clickOK(MainMenuPage())
            .startBlankForm("One Question Entity Update")
            .assertText("Ro-Ro Roy")
            .assertTextDoesNotExist("Roman Roy")
    }

    @Test
    fun entityListFormsAllShowAsUpdatedTogether() {
        testDependencies.server.apply {
            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )
        }

        val mainMenuPage = rule.withProject(testDependencies.server.url, matchExactly = true)

        testDependencies.server.apply {
            removeForm("One Question Entity Update")
            removeForm("One Question Entity Follow Up")

            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 2))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "people.csv", 2))
            )
        }

        mainMenuPage.clickFillBlankForm()
            .assertTextBesides(equalTo("One Question Entity Update"), containsString("Added on"))
            .assertTextBesides(equalTo("One Question Entity Follow Up"), containsString("Added on"))
            .clickRefresh()
            .assertTextBesides(equalTo("One Question Entity Update"), containsString("Updated on"))
            .assertTextBesides(
                equalTo("One Question Entity Follow Up"),
                containsString("Updated on")
            )
    }

    @Test
    fun aLocallyCreatedEntity_thatIsDeletedOnTheServer_isNotAvailableToFollowUpForms() {
        testDependencies.server.includeIntegrityUrl()
        testDependencies.server.addForm("one-question-entity-registration-id.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .also {
                testDependencies.server.deleteEntity("Logan Roy")
            }

            .clickFillBlankForm()
            .clickRefresh()

            .clickOnForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Logan Roy")
    }
}
