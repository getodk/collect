package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

@SuppressLint("ViewConstructor")
class GeoTraceWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val geoDataRequester: GeoDataRequester,
    private val dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails) {

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            val readOnly = questionDetails.isReadOnly
            val answer = GeoWidgetUtils.getGeoPolyAnswerToDisplay(prompt.answerText)
            val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                GeoTraceWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    answer,
                    readOnly,
                    buttonFontSize,
                    answerFontSize,
                    onGetLineClick = { geoDataRequester.requestGeoPoly(formEntryPrompt) },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? = formEntryPrompt.answerValue

    override fun clearAnswer() = Unit

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit
}
