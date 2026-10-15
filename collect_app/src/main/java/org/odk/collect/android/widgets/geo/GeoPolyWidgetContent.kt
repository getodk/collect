package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.icons.R
import org.odk.collect.maplibre.staticmap.StaticMapLoader
import org.odk.collect.strings.R.string

@Composable
fun GeoPolyWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    staticMapLoader: StaticMapLoader,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onGetGeoPolyClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isShape = formEntryPrompt.dataType == Constants.DATATYPE_GEOSHAPE

    Column {
        if (!readOnly || !answer.isNullOrEmpty()) {
            val buttonText = when {
                readOnly -> stringResource(if (isShape) string.view_polygon else string.view_line)
                answer.isNullOrEmpty() -> stringResource(if (isShape) string.get_polygon else string.get_line)
                else -> stringResource(if (isShape) string.view_or_change_polygon else string.view_or_change_line)
            }
            WidgetIconButton(
                ImageVector.vectorResource(if (isShape) R.drawable.ic_outline_polygon_white_24 else R.drawable.ic_outline_polyline_white_24),
                buttonText,
                buttonFontSize,
                onGetGeoPolyClick,
                onLongClick
            )
        }

        WidgetAnswer(
            Modifier.padding(top = marginStandard()),
            formEntryPrompt,
            answer,
            answerFontSize,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            staticMapLoader = staticMapLoader,
            onLongClick = onLongClick
        )
    }
}
