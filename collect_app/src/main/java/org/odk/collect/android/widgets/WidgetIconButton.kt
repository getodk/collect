package org.odk.collect.android.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

@Composable
fun WidgetIconButton(
    icon: ImageVector,
    text: String,
    fontSize: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            )
            .semantics { contentDescription = text }
            .padding(vertical = dimensionResource(id = dimen.margin_small)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = contentColor,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = dimen.margin_extra_small)))
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
