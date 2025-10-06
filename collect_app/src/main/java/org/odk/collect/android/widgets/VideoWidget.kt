package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.VideoWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.R
import timber.log.Timber
import java.io.File

@SuppressLint("ViewConstructor")
class VideoWidget(
    context: Context,
    questionDetails: QuestionDetails,
    dependencies: Dependencies,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    lateinit var binding: VideoWidgetBinding

    private var binaryName: String?

    init {
        binaryName = formEntryPrompt.getAnswerText()
        render()
    }

    override fun onCreateWidgetView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        binding = VideoWidgetBinding.inflate((context as Activity).layoutInflater)

        binding.recordVideoButton.setOnClickListener {
            getPermissionsProvider().requestCameraPermission(
                context,
                object : PermissionListener {
                    override fun granted() {
                        captureVideo()
                    }
                }
            )
        }
        binding.chooseVideoButton.setOnClickListener { chooseVideo() }
        binding.playVideoButton.isEnabled = binaryName != null
        binding.playVideoButton.setOnClickListener { playVideoFile() }

        if (formEntryPrompt.isReadOnly) {
            binding.recordVideoButton.visibility = GONE
            binding.chooseVideoButton.visibility = GONE
        }

        if (formEntryPrompt.appearanceHint != null &&
            formEntryPrompt.appearanceHint.lowercase().contains(Appearances.NEW)
        ) {
            binding.chooseVideoButton.visibility = GONE
        }

        return binding.getRoot()
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
        binding.playVideoButton.isEnabled = false
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return if (binaryName != null) {
            StringData(binaryName!!)
        } else {
            null
        }
    }

    override fun setData(answer: Any) {
        if (binaryName != null) {
            deleteFile()
        }

        if (answer is File) {
            if (answer.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPrompt.getIndex().toString(),
                    answer.absolutePath
                )
                binaryName = answer.name
                widgetValueChanged()
                binding.playVideoButton.isEnabled = binaryName != null
            } else {
                Timber.e(Error("Inserting Video file FAILED"))
            }
        } else {
            Timber.e(Error("VideoWidget's setBinaryData must receive a File or Uri object."))
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.recordVideoButton.setOnLongClickListener(l)
        binding.chooseVideoButton.setOnLongClickListener(l)
        binding.playVideoButton.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.recordVideoButton.cancelLongPress()
        binding.chooseVideoButton.cancelLongPress()
        binding.playVideoButton.cancelLongPress()
    }

    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val requestCode = RequestCodes.VIDEO_CAPTURE

        // request high resolution if configured for that...
        val highResolution =
            settingsProvider.getUnprotectedSettings().getBoolean(ProjectKeys.KEY_HIGH_RESOLUTION)
        if (highResolution) {
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        }

        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.getIndex())
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(
                    R.string.activity_not_found,
                    context.getString(R.string.capture_video)
                ), Toast.LENGTH_SHORT
            )
                .show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }

    private fun chooseVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.getIndex())
            (context as Activity).startActivityForResult(
                intent,
                RequestCodes.VIDEO_CHOOSER
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(
                    R.string.activity_not_found,
                    context.getString(R.string.choose_video)
                ), Toast.LENGTH_SHORT
            )
                .show()

            waitingForDataRegistry.cancelWaitingForData()
        }
    }

    private fun playVideoFile() {
        val file = questionMediaManager.getAnswerFile(binaryName)
        mediaUtils.openFile(context, file!!, "video/*")
    }
}
