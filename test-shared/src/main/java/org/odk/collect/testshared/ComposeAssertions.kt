package org.odk.collect.testshared

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText

@OptIn(ExperimentalTestApi::class)
object ComposeAssertions {
    fun assertVisible(composeRule: ComposeTestRule, text: String) {
        composeRule.run {
            waitUntilAtLeastOneExists(hasText(text))
            onNodeWithText(text).assertIsDisplayed()
        }
    }

    fun assertNotVisible(composeRule: ComposeTestRule, text: String) {
        composeRule.waitUntilDoesNotExist(hasText(text))
    }
}
