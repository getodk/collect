package org.odk.collect.android.widgets.video

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
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.strings.R
import timber.log.Timber
import java.io.File

@SuppressLint("ViewConstructor")
class ExVideoWidget(
    context: Context,
    questionDetails: QuestionDetails,
    dependencies: Dependencies,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val fileRequester: FileRequester
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    private var binaryName by mutableStateOf<String?>(formEntryPrompt.getAnswerText())

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)
        val viewModelProvider = ViewModelProvider(
            context as ComponentActivity,
            viewModelFactory {
                addInitializer(VideoWidgetAnswerViewModel::class) {
                    VideoWidgetAnswerViewModel(scheduler, questionMediaManager, mediaUtils)
                }
            }
        )

        return ComposeView(context).apply {
            setContextThemedContent {
                ExVideoWidgetContent(
                    readOnly,
                    buttonFontSize,
                    onLaunchClick = { launchExternalApp() },
                    onLongClick = { showContextMenu() }
                ) {
                    WidgetAnswer(
                        Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
                        formEntryPrompt,
                        binaryName,
                        viewModelProvider = viewModelProvider,
                        onLongClick = { showContextMenu() }
                    )
                }
            }
        }
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(
            formEntryPrompt.getIndex().toString(),
            questionMediaManager.getAnswerFile(binaryName)!!.absolutePath
        )
        binaryName = null
    }

    override fun clearAnswer() {
        deleteFile()
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return binaryName?.let { StringData(it) }
    }

    override fun setData(answer: Any) {
        if (binaryName != null) {
            deleteFile()
        }

        if (answer is File && mediaUtils.isVideoFile(answer)) {
            if (answer.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPrompt.getIndex().toString(),
                    answer.absolutePath
                )
                binaryName = answer.name
                widgetValueChanged()
            } else {
                Timber.e(Error("Inserting Video file FAILED"))
            }
        } else {
            if (answer is File) {
                showLongToast(R.string.invalid_file_type)
                mediaUtils.deleteMediaFile(answer.absolutePath)
                Timber.e(
                    Error(
                        "ExVideoWidget's setBinaryData must receive a video file but received: " + FileUtils.getMimeType(
                            answer
                        )
                    )
                )
            } else {
                Timber.e(Error("ExVideoWidget's setBinaryData must receive a video file but received: " + answer.javaClass))
            }
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    private fun launchExternalApp() {
        waitingForDataRegistry.waitForData(formEntryPrompt.getIndex())
        fileRequester.launch(
            context as Activity,
            ApplicationConstants.RequestCodes.EX_VIDEO_CHOOSER,
            formEntryPrompt
        )
    }
}
