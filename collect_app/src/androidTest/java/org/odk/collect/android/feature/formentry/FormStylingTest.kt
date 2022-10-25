package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

private const val FORM_FILE_NAME = "form_styling.xml"
private const val FORM_NAME = "Form styling"

class FormStylingTest {
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun questionLabelTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .assertText("Note text")
    }

    @Test
    fun requiredQuestionLabelWithHeaderStyleTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .swipeToNextQuestion("Required text question with header style", true)
    }

    @Test
    fun questionHintTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .assertText("Hint text")
    }

    @Test
    fun questionGuidanceTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .openProjectSettingsDialog()
            .clickSettings()
            .openFormManagement()
            .openShowGuidanceForQuestions()
            .clickOnString(R.string.guidance_yes)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm(FORM_NAME)
            .assertText("Guidance text")
            .clickOptionsIcon()
            .clickGeneralSettings()
            .openFormManagement()
            .openShowGuidanceForQuestions()
            .clickOnString(R.string.guidance_yes_collapsed)
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage(FORM_NAME))
            .clickOnText("Hint text")
            .assertText("Guidance text")
    }

    @Test
    fun selectOneWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectOneMinimalWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one minimal widget")
            .assertText("One")
            .openSelectMinimalDialog()
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectOneImageMapWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one image-map widget")
            .assertText("Selected: One")
    }

    @Test
    fun selectOneLabelWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one label widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectOneListWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one list widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectOneLikertWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .clickOnQuestion("Select one likert widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectMultipleWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectMultipleQuestions")
            .clickOnQuestion("Select multiple widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectMultipleMinimalWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectMultipleQuestions")
            .clickOnQuestion("Select multiple minimal widget")
            .assertText("One")
            .openSelectMinimalDialog()
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectMultipleImageMapWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectMultipleQuestions")
            .clickOnQuestion("Select multiple image-map widget")
            .assertText("Selected: One")
    }

    @Test
    fun selectMultipleLabelWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectMultipleQuestions")
            .clickOnQuestion("Select multiple label widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun selectMultipleListWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectMultipleQuestions")
            .clickOnQuestion("Select multiple list widget")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun rankWidgetTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnQuestion("Rank widget")
            .assertText("1. One\n2. Two\n3. Three")
            .clickOnText("Rank items")
            .assertText("One", "Two", "Three")
    }

    @Test
    fun hierarchyTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .assertHierarchyItem(0, "Note text", null)
            .assertHierarchyItem(1, "* Required text question with header style", null)
            .assertHierarchyItem(4, "Rank widget", "1. One")
    }

    @Test
    fun groupNameTest() {
        rule.startAtMainMenu()
            .copyForm(FORM_FILE_NAME)
            .startBlankForm(FORM_NAME)
            .clickGoToArrow()
            .clickOnGroup("selectOneQuestions")
            .assertText("selectOneQuestions")
            .clickOnQuestion("Select one widget")
            .assertText("selectOneQuestions")
    }
}
