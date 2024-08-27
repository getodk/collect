package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class PartialSubmissionTest {

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

        val root = XFormParser.getXMLDocument(submissions[0].inputStream().reader()).rootElement
        assertThat(root.name, equalTo("age"))
        assertThat(root.childCount, equalTo(1))
        assertThat(root.getChild(0), equalTo("123"))
    }
}
