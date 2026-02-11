package org.odk.collect.android.widgets.arbitraryfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

@SuppressLint("ViewConstructor")
class ExArbitraryFileWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val dependencies: Dependencies,
    questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val fileRequester: FileRequester,
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    private val arbitraryFileWidgetDelegate = ArbitraryFileWidgetDelegate(questionMediaManager)
    private var answer by mutableStateOf<String?>(formEntryPrompt.answerText)

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            val readOnly = questionDetails.isReadOnly
            val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

            setContextThemedContent {
                ExArbitraryFileWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    answer,
                    readOnly,
                    buttonFontSize,
                    answerFontSize,
                    onLaunchClick = { onButtonClick() },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer() = arbitraryFileWidgetDelegate.getAnswer(answer)

    override fun deleteFile() {
        arbitraryFileWidgetDelegate.deleteFile(formEntryPrompt.index.toString(), answer)
        answer = null
    }

    override fun setData(answer: Any) {
        arbitraryFileWidgetDelegate.setData(formEntryPrompt.index.toString(), this.answer, answer) {
            this.answer = it
            widgetValueChanged()
        }
    }

    override fun clearAnswer() {
        deleteFile()
        widgetValueChanged()
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    private fun onButtonClick() {
        waitingForDataRegistry.waitForData(formEntryPrompt.index)
        fileRequester.launch(
            (context as Activity), ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER,
            formEntryPrompt
        )
    }
}
