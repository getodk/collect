package org.odk.collect.android.feature.formentry.entities

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.StubOpenRosaServer.MediaFileItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

class ExternalCsvVsEntityListTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = chain(testDependencies).around(rule)

    /**
     * Regression test for [#6425](https://github.com/getodk/collect/issues/6425).
     *
     * When a form attaches a plain CSV whose name matches a project entity list, the form must read
     * from the attached CSV - not from the entity list of the same name.
     */
    @Test
    fun attachedCsvIsUsedInsteadOfEntityListOfTheSameName() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "external-csv-select.xml",
            listOf(MediaFileItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            // Creates a "people" entity list in the project, locally containing "Logan Roy".
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(QuestionAndAnswer("Name", "Logan Roy"))

            // Attaches a plain people.csv (containing "Roman Roy") that clashes with the "people" entity list by name.
            .startBlankForm("External Csv Select")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Logan Roy")
    }

    /**
     * An entity list is read from the database, not from the CSV it was originally seeded from
     * (that CSV is deleted after import). So entities created or updated offline - which exist only
     * in the database - remain visible.
     */
    @Test
    fun entityListIsReadFromDatabaseNotFromOriginalCsvFile() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(QuestionAndAnswer("Name", "Logan Roy"))
            .startBlankForm("One Question Entity Update")
            .assertTexts("Roman Roy", "Logan Roy")
    }
}
