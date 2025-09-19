package org.odk.collect.android.feature.formentry.dynamicpreload

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.StubOpenRosaServer.MediaFileItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

/**
 * This tests the ["Pull data from CSV" feature of XLSForms](https://xlsform.org/en/#how-to-pull-data-from-csv).
 *
 */
class DynamicPreLoadedDataPullTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun canUsePullDataFunctionToPullDataFromCSV() {
        testDependencies.server.addForm("pull_data.xml", listOf(MediaFileItem("fruits.csv")))

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("pull_data")
            .assertText("The fruit Mango is pulled csv data.")
    }

    @Test
    fun canUsePullDataFunctionToPullDataFromLocalEntities() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "entity-update-pulldata.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("Entity Update Pull Data")
            .clickOnText("Logan Roy")
            .swipeToNextQuestion("Name")
            .assertText("Logan Roy")
    }
}
