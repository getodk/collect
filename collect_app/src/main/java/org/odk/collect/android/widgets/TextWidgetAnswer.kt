package org.odk.collect.android.widgets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.sp
import org.odk.collect.android.utilities.HtmlUtils
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

@Composable
fun TextWidgetAnswer(
    modifier: Modifier,
    icon: ImageVector?,
    answer: String,
    fontSize: Int? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
    onClickLabel: String? = null
) {
    val annotatedAnswer = remember(answer) {
        AnnotatedString.fromHtml(HtmlUtils.markdownToHtml(answer))
    }
    val hasFormatting = annotatedAnswer.spanStyles.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        onClick()
                    }
                },
                onLongClick = onLongClick,
                onClickLabel = onClickLabel
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.padding(end = dimensionResource(id = dimen.margin_small)),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = dimen.high_emphasis.toFloat()
                )
            )
        }
        Text(
            text = annotatedAnswer,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize?.sp ?: MaterialTheme.typography.bodyLarge.fontSize,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (!hasFormatting) dimen.high_emphasis.toFloat() else 1f
                )
            )
        )
    }
}
