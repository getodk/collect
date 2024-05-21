package org.odk.collect.android.feature.entitymanagement

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.StubOpenRosaServer
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

class ViewEntitiesTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun canViewLocallyCreatedEntitiesInBrowser() {
        testDependencies.server.addForm("one-question-entity-registration.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            .addEntityListInBrowser("people")
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .openEntityBrowser()
            .clickOnList("people")
            .assertEntity("Logan Roy", "full_name: Logan Roy")
    }

    @Test
    fun canViewListEntitiesInBrowser() {
        testDependencies.server.addForm(
            "one-question-entity-follow-up.xml",
            listOf(StubOpenRosaServer.EntityListItem("people.csv"))
        )

        rule.withMatchExactlyProject(testDependencies.server.url)
            .addEntityListInBrowser("people")
            .refreshForms()
            .openEntityBrowser()
            .clickOnList("people")
            .assertEntity("Roman Roy", "full_name: Roman Roy")
    }
}
