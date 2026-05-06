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
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceSerializationTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val recentAppsRule = RecentAppsRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(recentAppsRule)
        .around(rule)

    @Test
    fun savingDraft_doesNotPruneNonRelevantNodes() {
        testDependencies.server.addForm("one-question-relevance.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Relevance")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .answerQuestion("what is your age", "30")
            .swipeToPreviousQuestion("Do you want to continue?")
            .clickOnText("No")
            .pressBackAndSaveAsDraft()
            .clickDrafts(1)
            .clickOnForm("One Question Relevance")
            .clickOnQuestion("Do you want to continue?")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .assertAnswer("what is your age", "30")
    }

    @Test
    fun savepoint_doesNotPruneNonRelevantNodes() {
        testDependencies.server.addForm("one-question-relevance.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Relevance")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .answerQuestion("what is your age", "30")
            .swipeToPreviousQuestion("Do you want to continue?")
            .clickOnText("No")

        recentAppsRule.leaveAndKillApp()

        rule.reopenApp()
            .startBlankFormWithSavepoint("One Question Relevance")
            .clickRecover(FormHierarchyPage("One Question Relevance"))
            .clickOnQuestion("Do you want to continue?")
            .clickOnText("Yes")
            .swipeToNextQuestion("what is your age")
            .assertAnswer("what is your age", "30")
    }

    @Test
    fun finalizingForm_prunesNonRelevantNodes() {
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
    fun finalizingAllDrafts_prunesNonRelevantNodes() {
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
