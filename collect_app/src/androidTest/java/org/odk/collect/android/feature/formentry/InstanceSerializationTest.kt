package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceSerializationTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun finalizingForm_doesPrunesNonRelevantNodes() {
        testDependencies.server.addForm("one-question-relevance.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Relevance")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .swipeToPreviousQuestion("Do you want to continue?")
            .clickOnText("No")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()

        val file = testDependencies.server.submissions[0]
        val instanceRootElement = XFormParser.getXMLDocument(file.inputStream().reader()).rootElement
        assertThat(instanceRootElement.indexOf(null, "age", 0), equalTo(-1))
    }

    @Test
    fun finalizingAllDrafts_doesPrunesNonRelevantNodes() {
        testDependencies.server.addForm("one-question-relevance.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Relevance")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .swipeToPreviousQuestion("Do you want to continue?")
            .clickOnText("No")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .pressBack(MainMenuPage())
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()

        val file = testDependencies.server.submissions[0]
        val instanceRootElement = XFormParser.getXMLDocument(file.inputStream().reader()).rootElement
        assertThat(instanceRootElement.indexOf(null, "age", 0), equalTo(-1))
    }
}
