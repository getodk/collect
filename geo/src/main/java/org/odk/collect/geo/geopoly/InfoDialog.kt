package org.odk.collect.geo.geopoly

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

object InfoDialog {
    data class InfoItem(
        val icon: ImageVector,
        val text: String
    )

    enum class Type {
        PLACEMENT_FROM_SNACKBAR,
        PLACEMENT_FROM_INFO_BUTTON,
        MANUAL_FROM_SNACKBAR,
        MANUAL_FROM_INFO_BUTTON,
    }

    fun show(context: Context, type: Type) {
        var dialog: AlertDialog? = null

        val info = ComposeView(context).apply {
            setContextThemedContent {
                when (type) {
                    Type.PLACEMENT_FROM_SNACKBAR -> PlacementFromSnackbarInfo { dialog?.dismiss() }
                    Type.PLACEMENT_FROM_INFO_BUTTON -> PlacementFromInfoButtonInfo { dialog?.dismiss() }
                    Type.MANUAL_FROM_SNACKBAR -> ManualFromSnackbarInfo { dialog?.dismiss() }
                    Type.MANUAL_FROM_INFO_BUTTON -> ManualFromInfoButtonInfo { dialog?.dismiss() }
                }
            }
        }

        dialog = MaterialAlertDialogBuilder(context)
            .setView(info)
            .show()
    }
}

@Composable
private fun PlacementFromSnackbarInfo(onDone: () -> Unit) {
    InfoContent(
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Long press to move point"),
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, "Remove last point"),
        InfoDialog.InfoItem(Icons.Filled.Delete, "Delete shape to start over"),
        InfoDialog.InfoItem(Icons.Filled.AddLocation, "Add point"),
        onDone = onDone
    )
}

@Composable
private fun PlacementFromInfoButtonInfo(onDone: () -> Unit) {
    InfoContent(
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Tap to add a point"),
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Long press to move point"),
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, "Remove last point"),
        InfoDialog.InfoItem(Icons.Filled.Delete, "Delete entire shape"),
        onDone = onDone
    )
}

@Composable
private fun ManualFromSnackbarInfo(onDone: () -> Unit) {
    InfoContent(
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.DirectionsWalk, "Physically move to correct"),
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Long press to move point"),
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, "Remove last point"),
        InfoDialog.InfoItem(Icons.Filled.Delete, "Delete entire shape"),
        onDone = onDone
    )
}

@Composable
private fun ManualFromInfoButtonInfo(onDone: () -> Unit) {
    InfoContent(
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Tap to add a point"),
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.DirectionsWalk, "Physically move to correct"),
        InfoDialog.InfoItem(Icons.Filled.TouchApp, "Long press to move point"),
        InfoDialog.InfoItem(Icons.AutoMirrored.Filled.Backspace, "Remove last point"),
        InfoDialog.InfoItem(Icons.Filled.Delete, "Delete entire shape"),
        onDone = onDone
    )
}

@Composable
private fun Title() {
    Text(
        modifier = Modifier.padding(
            start = dimensionResource(id = dimen.margin_standard),
            top = dimensionResource(id = dimen.margin_extra_small),
            bottom = dimensionResource(id = dimen.margin_standard)
        ),
        text = "How to modify the map",
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
private fun InfoContent(
    vararg items: InfoDialog.InfoItem,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.padding(dimensionResource(id = dimen.margin_standard))
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
private fun DoneButton(onDone: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(id = dimen.margin_standard)),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDone) {
            Text("Done")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlacementFromSnackbarInfoPreview() {
    PlacementFromSnackbarInfo {}
}

@Preview(showBackground = true)
@Composable
private fun PlacementFromInfoButtonInfoPreview() {
    PlacementFromInfoButtonInfo {}
}

@Preview(showBackground = true)
@Composable
private fun ManualFromSnackbarInfoPreview() {
    ManualFromSnackbarInfo {}
}

@Preview(showBackground = true)
@Composable
private fun ManualFromInfoButtonInfoPreview() {
    ManualFromInfoButtonInfo {}
}
