package org.odk.collect.android.widgets.geo

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
    val buttonDetails = if (formEntryPrompt.dataType == Constants.DATATYPE_GEOSHAPE) GeoPolyButtonDetails.SHAPE else GeoPolyButtonDetails.TRACE

    Column {
        if (!readOnly || !answer.isNullOrEmpty()) {
            val buttonText = when {
                readOnly -> stringResource(buttonDetails.view)
                answer.isNullOrEmpty() -> stringResource(buttonDetails.get)
                else -> stringResource(buttonDetails.viewOrChange)
            }
            WidgetIconButton(
                ImageVector.vectorResource(buttonDetails.icon),
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

private enum class GeoPolyButtonDetails(
    @DrawableRes val icon: Int,
    @StringRes val get: Int,
    @StringRes val view: Int,
    @StringRes val viewOrChange: Int
) {
    TRACE(R.drawable.ic_outline_polyline_white_24, string.get_line, string.view_line, string.view_or_change_line),
    SHAPE(R.drawable.ic_outline_polygon_white_24, string.get_polygon, string.view_polygon, string.view_or_change_polygon)
}
