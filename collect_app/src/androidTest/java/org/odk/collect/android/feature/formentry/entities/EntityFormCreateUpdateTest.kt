package org.odk.collect.android.feature.formentry.entities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class EntityFormCreateUpdateTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Logan Roy")
    }

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpFormsWithCachedFormDefs() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update") // Open to create cached form def
            .pressBackAndDiscardForm()

            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Logan Roy")
    }

    @Test
    fun fillingEntityUpdateForm_updatesEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertText("Romulus Roy")
            .assertTextDoesNotExist("Roman Roy")
    }

    @Test
    fun fillingEntityCreateForm_withUpdate_doesNotCreateEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-create-and-update.xml")
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
    fun fillingEntityUpdateForm_withCreate_doesNotUpdateEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update-and-create.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertTextDoesNotExist("Romulus Roy")
            .assertText("Roman Roy")
    }

    /**
     * Entity IDs are often generated on the first load of a form instance (using the
     * odk-instance-first-load event). If the node containing the ID is non-relevant when a draft is
     * saved, it should still be included in that draft. If it's not, the ID will be lost when
     * reloading the draft (as odk-instance-first-load won't fire again).
     */
    @Test
    fun entityIdGeneratedOnFirstLoad_isPreserved_whenSavingDraftWhileEntityNodeIsNonRelevant() {
        testDependencies.server.addForm("entity-registration-with-relevance.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("Entity registration with relevance")
            .pressBackAndSaveAsDraft()
            .clickDrafts()
            .clickOnForm("Entity registration with relevance")
            .clickOnQuestion("Do you want to continue?")
            .clickOnText("Yes")
            .swipeToNextQuestion("Name")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()

        val file = testDependencies.server.submissions[0]
        val instanceRootElement = XFormParser.getXMLDocument(file.inputStream().reader()).rootElement
        val entityElement = instanceRootElement
            .getElement(null, "participant")
            .getElement(null, "meta")
            .getElement(null, "entity")
        val id = entityElement.getAttributeValue(null, "id")

        assertThat(id.isBlank(), equalTo(false))
    }
}
