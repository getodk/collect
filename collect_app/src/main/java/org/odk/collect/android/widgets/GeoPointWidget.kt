package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.geo.GeoUtils

@SuppressLint("ViewConstructor")
class GeoPointWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val geoDataRequester: GeoDataRequester,
    private val dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {

    private var answer by mutableStateOf<String?>(questionDetails.prompt.answerText)

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            val readOnly = questionDetails.isReadOnly
            val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                GeoPointWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    prompt,
                    answer,
                    readOnly,
                    buttonFontSize,
                    answerFontSize,
                    onGetPointClick = { geoDataRequester.requestGeoPoint(prompt, waitingForDataRegistry) },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        val parsedGeometryPoint = GeoUtils.parseGeometryPoint(answer)
        return if (parsedGeometryPoint == null) {
            null
        } else {
            GeoPointData(parsedGeometryPoint)
        }
    }

    override fun clearAnswer() {
        answer = null
        widgetValueChanged()
    }

    override fun setData(answer: Any) {
        this.answer = answer.toString()
        widgetValueChanged()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit
}
