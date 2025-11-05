package org.odk.collect.android.widgets.barcode

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.icons.R

@Composable
fun BarcodeWidgetAnswer(
    modifier: Modifier,
    answer: String,
    fontSize: Int,
    onLongClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_baseline_barcode_scanner_white_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = dimen.margin_small)))
        Text(
            text = answer,
            style = TextStyle(fontSize = fontSize.sp)
        )
    }
}
