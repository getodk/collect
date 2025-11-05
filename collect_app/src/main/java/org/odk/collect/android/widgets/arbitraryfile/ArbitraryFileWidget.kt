package org.odk.collect.android.widgets.arbitraryfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import timber.log.Timber
import java.io.File

@SuppressLint("ViewConstructor")
class ArbitraryFileWidget(
    context: Context,
    questionDetails: QuestionDetails,
    dependencies: Dependencies,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
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
                    readOnly,
                    buttonFontSize,
                    onChooseFileClick = { onButtonClick() },
                    onLongClick = { showContextMenu() }
                ) {
                    WidgetAnswer(
                        Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
                        formEntryPrompt,
                        answer,
                        answerFontSize,
                        viewModelProvider,
                        onLongClick = { showContextMenu() }
                    )
                }
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        return if (answer.isNullOrEmpty()) null else StringData(answer!!)
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(
            formEntryPrompt.index.toString(),
            questionMediaManager.getAnswerFile(answer)!!.absolutePath
        )
        answer = null
    }

    override fun setData(answer: Any) {
        if (this.answer != null) {
            deleteFile()
        }

        if (answer is File) {
            if (answer.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPrompt.index.toString(),
                    answer.absolutePath
                )
                this.answer = answer.name
                widgetValueChanged()
            } else {
                Timber.e(Error("Inserting Arbitrary file FAILED"))
            }
        } else {
            Timber.e(Error("FileWidget's setBinaryData must receive a File but received: " + answer.javaClass))
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
