package org.odk.collect.geo.geopoly

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class InfoContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `shows dialog content from snackbar in PLACEMENT mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.PLACEMENT)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = true) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_shape_to_start_over_info_item,
                org.odk.collect.strings.R.string.add_point_info_item,
            )
        )
    }

    @Test
    fun `shows dialog content from info button in PLACEMENT mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.PLACEMENT)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = false) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.tap_to_add_a_point_info_item,
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    @Test
    fun `shows dialog content from snackbar in MANUAL mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.MANUAL)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = true) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    @Test
    fun `shows dialog content from info button in MANUAL mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.MANUAL)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = false) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    @Test
    fun `shows dialog content from snackbar in AUTOMATIC mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.AUTOMATIC)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = true) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    @Test
    fun `shows dialog content from info button in AUTOMATIC mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.AUTOMATIC)
        }

        composeTestRule.setContent {
            InfoContent(viewModel, fromSnackbar = false) {}
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    @Test
    fun `calls onDone when Done button is clicked`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.PLACEMENT)
        }

        var onDoneCalled = false

        composeTestRule.setContent {
            InfoContent(viewModel, false) { onDoneCalled = true }
        }

        composeTestRule
            .onNodeWithText(context.getString(org.odk.collect.strings.R.string.done))
            .assertIsDisplayed()
            .performClick()

        assertThat(onDoneCalled, equalTo(true))
    }

    private fun assertInfo(items: List<Int>) {
        items.forEach {
            composeTestRule
                .onNodeWithText(context.getString(it))
                .performScrollTo()
                .assertIsDisplayed()
        }
    }
}
