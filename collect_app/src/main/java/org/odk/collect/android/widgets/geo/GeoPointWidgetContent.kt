package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetIconButton
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.geo.GeoUtils
import org.odk.collect.strings.R.string

@Composable
fun GeoPointWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonFontSize: Int,
    answerFontSize: Int,
    onGetPointClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val hasAnswer = remember(answer) { GeoUtils.parseGeometryPoint(answer) != null }

    Column {
        if (!readOnly || hasAnswer) {
            val buttonText = when {
                !hasAnswer -> string.get_point
                readOnly -> string.view_point
                Appearances.isGeoPointMapAppearance(formEntryPrompt) -> string.view_or_change_point
                else -> string.change_point
            }

            WidgetIconButton(
                Icons.Default.MyLocation,
                stringResource(buttonText),
                buttonFontSize,
                onGetPointClick,
                onLongClick
            )
        }

        WidgetAnswer(
            Modifier.padding(top = marginStandard()),
            formEntryPrompt,
            answer,
            answerFontSize,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            onLongClick = onLongClick
        )
    }
}
