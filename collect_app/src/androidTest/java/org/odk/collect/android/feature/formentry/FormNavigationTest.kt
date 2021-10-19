/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage

class FormNavigationTest {
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(ResetStateRule())
        .around(rule)

    @Test // TestCase14
    fun showsAndHidesButtonsCorrectlyOnEachScreen() {
        rule.startAtMainMenu()
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .assertQuestion("What is your name?")
            .assertTextNotDisplayed(R.string.form_backward)
            .clickForwardButton()
            .assertQuestion("What is your age?")
            .clickBackwardButton()
            .assertQuestion("What is your name?")
            .clickForwardButton()
            .assertQuestion("What is your age?")
            .clickForwardButtonToEndScreen()
            .assertText(R.string.form_backward)
            .assertTextNotDisplayed(R.string.form_forward)
    }

    @Test
    fun whenNavigatingBackwardsIsDisabled_showsAndHidesButtonsCorrectlyOnEachScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickMovingBackwards()
            .clickOnString(R.string.yes)
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .assertQuestion("What is your name?")
            .assertTextNotDisplayed(R.string.form_backward)
            .clickForwardButton()
            .assertQuestion("What is your age?")
            .assertTextNotDisplayed(R.string.form_backward)
            .clickForwardButtonToEndScreen()
            .assertTextNotDisplayed(R.string.form_backward)
            .assertTextNotDisplayed(R.string.form_forward)
    }

    @Test
    fun whenButtonsDisabled_buttonsNotShown() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickSwipes()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .assertTextNotDisplayed(R.string.form_backward)
            .assertTextNotDisplayed(R.string.form_forward)
            .swipeToNextQuestion("What is your age?")
            .assertTextNotDisplayed(R.string.form_backward)
            .assertTextNotDisplayed(R.string.form_forward)
            .swipeToEndScreen()
            .assertTextNotDisplayed(R.string.form_backward)
            .assertTextNotDisplayed(R.string.form_forward)
    }

    @Test
    fun whenNavigationSettingsChangeChangesShouldBeReflectedInFormFilling() {
        rule.startAtMainMenu()
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .assertQuestion("What is your name?")

            // assert that 'Swipes and buttons' mode is enabled
            .swipeToNextQuestion("What is your age?")
            .clickBackwardButton()
            .assertText("What is your name?")

            // change settings to 'Horizontal swipes' mode'
            .clickOptionsIcon()
            .clickGeneralSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickSwipes()
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage("Two Question"))

            // assert that 'Horizontal swipes' mode is enabled
            .swipeToNextQuestion("What is your age?")
            .assertNavigationButtonsAreHidden()

            // change settings to 'Forward/backward buttons' mode'
            .clickOptionsIcon()
            .clickGeneralSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickUseNavigationButtons()
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage("Two Question"))

            // assert that 'Forward/backward buttons' mode is enabled
            .swipeToPreviousQuestion("What is your age?")
            .clickBackwardButton()
            .assertText("What is your name?")

            // change settings to 'Swipes and buttons' mode'
            .clickOptionsIcon()
            .clickGeneralSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickUseSwipesAndButtons()
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage("Two Question"))

            // assert that 'Swipes and buttons' mode is enabled
            .swipeToNextQuestion("What is your age?")
            .clickBackwardButton()
            .assertText("What is your name?")
    }
}
