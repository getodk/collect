package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import java.io.File
import java.io.StringReader

@RunWith(AndroidJUnit4::class)
class PartialSubmissionTet {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun canFillAndSubmitAFormWithPartialSubmission() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question-partial.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("what is your age", "123"))

            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()

        val submissions = testDependencies.server.submissions
        assertThat(submissions.size, equalTo(1))

        val root = parseXml(submissions[0]).rootElement
        assertThat(root.name, equalTo("age"))
        assertThat(root.childCount, equalTo(1))
        assertThat(root.getChild(0), equalTo("123"))
    }

    private fun parseXml(file: File): Document {
        return StringReader(String(file.readBytes())).use { reader ->
            val parser = KXmlParser()
            parser.setInput(reader)
            Document().also { it.parse(parser) }
        }
    }
}
