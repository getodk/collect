package org.odk.collect.android.support.rules

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.rules.ExternalResource
import org.odk.collect.android.support.pages.Page

/**
 * Wrapper around a ComposeTestRule that automatically assigns Page.composeRule
 * before the test.
 */
class PageComposeRule(
    val composeRule: ComposeTestRule = createEmptyComposeRule()
) : ExternalResource() {

    override fun before() {
        Page.composeRule = composeRule
    }

    override fun after() {
        Page.composeRule = null
    }
}
