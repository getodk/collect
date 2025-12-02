package org.odk.collect.android.widgets.arbitraryfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

@SuppressLint("ViewConstructor")
class ArbitraryFileWidget(
    context: Context,
    questionDetails: QuestionDetails,
    dependencies: Dependencies,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    private val arbitraryFileWidgetDelegate = ArbitraryFileWidgetDelegate(questionMediaManager)
    private var answer by mutableStateOf<String?>(formEntryPrompt.answerText)

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val viewModelProvider = ViewModelProvider(
            context as ComponentActivity,
            viewModelFactory {
                addInitializer(ArbitraryFileWidgetAnswerViewModel::class) {
                    ArbitraryFileWidgetAnswerViewModel(questionMediaManager, mediaUtils)
                }
            }
        )

        return ComposeView(context).apply {
            val readOnly = questionDetails.isReadOnly
            val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

            setContextThemedContent {
                ArbitraryFileWidgetContent(
                    viewModelProvider,
                    formEntryPrompt,
                    answer,
                    readOnly,
                    buttonFontSize,
                    answerFontSize,
                    onChooseFileClick = { onButtonClick() },
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

    private fun onButtonClick() {
        waitingForDataRegistry.waitForData(formEntryPrompt.index)
        mediaUtils.pickFile(
            (context as Activity),
            "*/*",
            ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER
        )
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit
}
