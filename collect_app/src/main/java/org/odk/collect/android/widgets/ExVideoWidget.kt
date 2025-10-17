package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.ExVideoWidgetAnswerBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
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
    lateinit var binding: ExVideoWidgetAnswerBinding
    var answerFile: File? = null

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        setupAnswerFile(prompt.getAnswerText())

        binding = ExVideoWidgetAnswerBinding.inflate((context as Activity).layoutInflater)

        binding.captureVideoButton.visibility = if (questionDetails.isReadOnly) GONE else VISIBLE
        binding.captureVideoButton.setOnClickListener { view: View? -> launchExternalApp() }
        binding.playVideoButton.setOnClickListener { view: View? ->
            mediaUtils.openFile(
                getContext(),
                answerFile!!,
                "video/*"
            )
        }
        binding.playVideoButton.isEnabled = answerFile != null

        return binding.getRoot()
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(
            formEntryPrompt.getIndex().toString(),
            answerFile!!.absolutePath
        )
        answerFile = null
    }

    override fun clearAnswer() {
        deleteFile()
        binding.playVideoButton.isEnabled = false
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return if (answerFile != null) {
            StringData(answerFile!!.name)
        } else {
            null
        }
    }

    override fun setData(answer: Any) {
        if (answerFile != null) {
            clearAnswer()
        }

        if (answer is File && mediaUtils.isVideoFile(answer)) {
            answerFile = answer
            if (answerFile!!.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPrompt.getIndex().toString(),
                    answerFile!!.absolutePath
                )
                binding.playVideoButton.isEnabled = true
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

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.captureVideoButton.setOnLongClickListener(l)
        binding.playVideoButton.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureVideoButton.cancelLongPress()
        binding.playVideoButton.cancelLongPress()
    }

    private fun launchExternalApp() {
        waitingForDataRegistry.waitForData(formEntryPrompt.getIndex())
        fileRequester.launch(
            context as Activity,
            ApplicationConstants.RequestCodes.EX_VIDEO_CHOOSER,
            formEntryPrompt
        )
    }

    private fun setupAnswerFile(fileName: String?) {
        if (fileName != null && !fileName.isEmpty()) {
            answerFile = questionMediaManager.getAnswerFile(fileName)
        }
    }
}
