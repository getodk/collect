package org.odk.collect.entities.browser

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.entities.storage.Entity

@RunWith(AndroidJUnit4::class)
class EntityItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `sorts order of properties`() {
        composeTestRule.setContent {
            EntityItem(
                entity = Entity.Saved(
                    "1",
                    "S.D.O.S",
                    properties = listOf(Pair("name", "S.D.O.S"), Pair("length", "2:50")),
                    index = 0
                )
            )
        }

        composeTestRule.onNodeWithText("length: 2:50\nname: S.D.O.S").assertIsDisplayed()
    }

    @Test
    fun `shows offline pill when entity is offline`() {
        composeTestRule.setContent {
            EntityItem(
                entity = Entity.Saved("1", "S.D.O.S", index = 0, state = Entity.State.OFFLINE)
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(org.odk.collect.strings.R.string.offline))
            .assertIsDisplayed()
    }

    @Test
    fun `does not show offline pill when entity is online`() {
        composeTestRule.setContent {
            EntityItem(
                entity = Entity.Saved("1", "S.D.O.S", index = 0, state = Entity.State.ONLINE)
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(org.odk.collect.strings.R.string.offline))
            .assertIsNotDisplayed()
    }

    @Test
    fun `shows id and version`() {
        composeTestRule.setContent {
            EntityItem(
                entity = Entity.Saved("1", "S.D.O.S", version = 11, index = 0)
            )
        }

        composeTestRule.onNodeWithText("1 (11)").assertIsDisplayed()
    }
}
