package org.odk.collect.testshared

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule

@OptIn(ExperimentalTestApi::class)
object ComposeAssertions {
    fun assertVisible(composeRule: ComposeTestRule, text: String) {
        assertVisible(composeRule, hasText(text))
    }

    fun assertVisible(composeRule: ComposeTestRule, matcher: SemanticsMatcher) {
        composeRule.run {
            waitUntilAtLeastOneExists(matcher)
            onNode(matcher).assertIsDisplayed()
        }
    }

    fun assertNotVisible(composeRule: ComposeTestRule, matcher: SemanticsMatcher) {
        composeRule.waitUntilDoesNotExist(matcher)
    }
}
