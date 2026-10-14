package org.odk.collect.android.widgets.geo

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.maplibre.staticmap.MapLibreStaticMapLoader
import org.odk.collect.maplibre.staticmap.StaticMapLoader

@SuppressLint("ViewConstructor")
class GeoTraceWidget @JvmOverloads constructor(
    context: Context,
    questionDetails: QuestionDetails,
    private val geoDataRequester: GeoDataRequester,
    private val dependencies: Dependencies,
    private val staticMapLoader: StaticMapLoader = MapLibreStaticMapLoader(context)
) : QuestionWidget(context, dependencies, questionDetails) {

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                GeoTraceWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    staticMapLoader,
                    prompt,
                    prompt.answerText,
                    questionDetails.isReadOnly,
                    QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE),
                    answerFontSize,
                    onGetLineClick = { geoDataRequester.requestGeoPoly(prompt) },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? = formEntryPrompt.answerValue

    override fun clearAnswer() = Unit

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit
}
