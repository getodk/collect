package org.odk.collect.geo.geopoly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.testshared.DummyActivity

@RunWith(AndroidJUnit4::class)
class InfoDialogTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<DummyActivity>()

    @Test
    fun `shows dialog content from snackbar in PLACEMENT mode`() {
        val viewModel = mock<GeoPolyViewModel>().apply {
            whenever(recordingMode).thenReturn(GeoPolyViewModel.RecordingMode.PLACEMENT)
        }

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = true
            )
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

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = false
            )
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

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = true
            )
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.physically_move_to_correct_info_item,
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

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = false
            )
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.tap_to_add_a_point_info_item,
                org.odk.collect.strings.R.string.physically_move_to_correct_info_item,
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

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = true
            )
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.physically_move_to_correct_info_item,
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

        composeRule.activityRule.scenario.onActivity { activity ->
            InfoDialog.show(
                context = activity,
                viewModel = viewModel,
                fromSnackbar = false
            )
        }

        assertInfo(
            listOf(
                org.odk.collect.strings.R.string.tap_to_add_a_point_info_item,
                org.odk.collect.strings.R.string.physically_move_to_correct_info_item,
                org.odk.collect.strings.R.string.long_press_to_move_point_info_item,
                org.odk.collect.strings.R.string.remove_last_point_info_item,
                org.odk.collect.strings.R.string.delete_entire_shape_info_item,
            )
        )
    }

    private fun assertInfo(items: List<Int>) {
        items.forEach {
            composeRule
                .onNodeWithText(composeRule.activity.getString(it))
                .performScrollTo()
                .assertIsDisplayed()
        }
    }
}
