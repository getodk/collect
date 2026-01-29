package org.odk.collect.geo.geopoly

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.strings.R.string

object InfoDialog {
    data class InfoItem(
        val icon: ImageVector,
        val text: String
    )

    fun show(context: Context, viewModel: GeoPolyViewModel, fromSnackbar: Boolean) {
        var dialog: AlertDialog? = null

        val info = ComposeView(context).apply {
            setContextThemedContent {
                InfoContent(viewModel, fromSnackbar) { dialog?.dismiss() }
            }
        }

        dialog = MaterialAlertDialogBuilder(context)
            .setView(info)
            .show()
    }
}

@Composable
fun InfoContent(
    viewModel: GeoPolyViewModel,
    fromSnackbar: Boolean,
    onDone: () -> Unit
) {
    val items = when (viewModel.recordingMode) {
        GeoPolyViewModel.RecordingMode.PLACEMENT -> {
            if (fromSnackbar) {
                listOf(
                    InfoDialog.InfoItem(Icons.Filled.TouchApp, stringResource(string.long_press_to_move_point_info_item)),
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, stringResource(string.remove_last_point_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.Delete, stringResource(string.delete_shape_to_start_over_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.AddLocation, stringResource(string.add_point_info_item))
                )
            } else {
                listOf(
                    InfoDialog.InfoItem(Icons.Filled.AddLocation, stringResource(string.tap_to_add_a_point_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.TouchApp, stringResource(string.long_press_to_move_point_info_item)),
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, stringResource(string.remove_last_point_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.Delete, stringResource(string.delete_entire_shape_info_item))
                )
            }
        }
        GeoPolyViewModel.RecordingMode.MANUAL, GeoPolyViewModel.RecordingMode.AUTOMATIC -> {
            if (fromSnackbar) {
                listOf(
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.DirectionsWalk, stringResource(string.physically_move_to_correct_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.TouchApp, stringResource(string.long_press_to_move_point_info_item)),
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, stringResource(string.remove_last_point_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.Delete, stringResource(string.delete_entire_shape_info_item)),
                )
            } else {
                listOf(
                    InfoDialog.InfoItem(Icons.Filled.AddLocation, stringResource(string.tap_to_add_a_point_info_item)),
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.DirectionsWalk, stringResource(string.physically_move_to_correct_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.TouchApp, stringResource(string.long_press_to_move_point_info_item)),
                    InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, stringResource(string.remove_last_point_info_item)),
                    InfoDialog.InfoItem(Icons.Filled.Delete, stringResource(string.delete_entire_shape_info_item))
                )
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(dimensionResource(id = dimen.margin_standard))
            .verticalScroll(scrollState)
    ) {
        Title()
        items.forEachIndexed { index, item ->
            Info(item.icon, item.text)
            if (index < items.lastIndex) {
                HorizontalDivider(
                    Modifier.padding(horizontal = dimensionResource(id = dimen.margin_small))
                )
            }
        }
        DoneButton(onDone)
    }
}

@Composable
private fun Title() {
    Text(
        modifier = Modifier.padding(
            start = dimensionResource(id = dimen.margin_standard),
            top = dimensionResource(id = dimen.margin_extra_small),
            bottom = dimensionResource(id = dimen.margin_standard)
        ),
        text = stringResource(string.how_to_modify_map),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun Info(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = dimen.margin_standard)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(start = dimensionResource(id = dimen.margin_small)),
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DoneButton(onDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = dimen.margin_standard)),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDone) {
            Text(stringResource(string.done))
        }
    }
}
