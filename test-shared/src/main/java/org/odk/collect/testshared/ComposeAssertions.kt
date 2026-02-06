package org.odk.collect.testshared

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText

object ComposeAssertions {
    fun assertVisible(composeRule: ComposeTestRule, text: String) {
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun assertNotVisible(composeRule: ComposeTestRule, text: String) {
        composeRule.onNodeWithText(text).assertIsNotDisplayed()
    }
}
