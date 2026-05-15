package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.ImageWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileAnswerDelegate
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.imageloader.GlideImageLoader
import org.odk.collect.selfiecamera.CaptureSelfieActivity
import org.odk.collect.permissions.PermissionListener
import java.util.Locale

@SuppressLint("ViewConstructor")
class ImageWidget @JvmOverloads constructor(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val tmpImageFilePath: String,
    dependencies: Dependencies,
    private val fileAnswerDelegate: FileAnswerDelegate = FileAnswerDelegate(questionMediaManager, questionDetails.prompt)
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {

    val binding: ImageWidgetBinding = ImageWidgetBinding.inflate((context as Activity).layoutInflater)

    private val selfie: Boolean = Appearances.isFrontCameraAppearance(formEntryPrompt)

    init {
        render()
        setupEventListeners()
        updateAnswer()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val appearance = prompt.appearanceHint
        if (selfie || appearance?.lowercase(Locale.ENGLISH)?.contains(Appearances.NEW) == true) {
            binding.chooseButton.visibility = GONE
        }

        if (questionDetails.isReadOnly) {
            binding.captureButton.visibility = GONE
            binding.chooseButton.visibility = GONE
        }

        return binding.root
    }

    override fun getAnswer(): IAnswerData? {
        return fileAnswerDelegate.getAnswer()
    }

    override fun clearAnswer() {
        fileAnswerDelegate.deleteFile()
        binding.captureButton.setText(org.odk.collect.strings.R.string.capture_image)
        binding.image.visibility = GONE
        binding.errorMessage.visibility = GONE
        widgetValueChanged()
    }

    override fun deleteFile() {
        fileAnswerDelegate.deleteFile()
    }

    override fun setData(answer: Any) {
        fileAnswerDelegate.setData(answer) {
            updateAnswer()
            widgetValueChanged()
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        binding.captureButton.setOnLongClickListener(listener)
        binding.chooseButton.setOnLongClickListener(listener)
        binding.image.setOnLongClickListener(listener)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureButton.cancelLongPress()
        binding.chooseButton.cancelLongPress()
        binding.image.cancelLongPress()
    }

    private fun setupEventListeners() {
        binding.captureButton.setOnClickListener {
            permissionsProvider.requestCameraPermission(context as Activity, object : PermissionListener {
                override fun granted() {
                    captureImage()
                }
            })
        }

        binding.chooseButton.setOnClickListener { chooseImage() }

        binding.image.setOnClickListener {
            fileAnswerDelegate.binaryName?.let { fileName ->
                val file = questionMediaManager.getAnswerFile(fileName)
                if (file != null) {
                    mediaUtils.openFile(context, file, "image/*")
                }
            }
        }
    }

    private fun updateAnswer() {
        binding.image.visibility = GONE
        binding.errorMessage.visibility = GONE

        fileAnswerDelegate.binaryName?.let { fileName ->
            val file = fileAnswerDelegate.getFile(fileName)
            if (file != null && file.exists()) {
                binding.image.visibility = VISIBLE
                imageLoader.loadImage(binding.image, file, ImageView.ScaleType.FIT_CENTER, object : GlideImageLoader.ImageLoaderCallback {
                    override fun onLoadFailed() {
                        binding.image.visibility = GONE
                        binding.errorMessage.visibility = VISIBLE
                    }

                    override fun onLoadSucceeded() {}
                })
            }
        }
    }

    private fun captureImage() {
        if (selfie && CameraUtils().isFrontCameraAvailable(context)) {
            val intent = Intent(context, CaptureSelfieActivity::class.java).apply {
                putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE))
            }
            launchActivityForResult(intent, RequestCodes.MEDIA_FILE_PATH, org.odk.collect.strings.R.string.capture_image)
        } else {
            val intent = ImageCaptureIntentCreator.imageCaptureIntent(formEntryPrompt, context, tmpImageFilePath)
            launchActivityForResult(intent, RequestCodes.IMAGE_CAPTURE, org.odk.collect.strings.R.string.capture_image)
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        launchActivityForResult(intent, RequestCodes.IMAGE_CHOOSER, org.odk.collect.strings.R.string.choose_image)
    }

    private fun launchActivityForResult(intent: Intent, requestCode: Int, errorStringResource: Int) {
        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.index)
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(org.odk.collect.strings.R.string.activity_not_found, context.getString(errorStringResource)),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
