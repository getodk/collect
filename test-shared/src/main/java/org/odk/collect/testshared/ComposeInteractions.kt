package org.odk.collect.testshared

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick

object ComposeInteractions {
    fun clickOn(composeRule: ComposeTestRule, matcher: SemanticsMatcher, assertion: () -> Unit) {
        composeRule.onNode(matcher).performClick()
        assertion()
    }
}