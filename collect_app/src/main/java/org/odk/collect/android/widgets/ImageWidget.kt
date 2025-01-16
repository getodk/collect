package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.ImageWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.Appearances.isFrontCameraAppearance
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator.imageCaptureIntent
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.selfiecamera.CaptureSelfieActivity
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class ImageWidget(
    context: Context,
    prompt: QuestionDetails,
    questionMediaManager: QuestionMediaManager,
    waitingForDataRegistry: WaitingForDataRegistry,
    tmpImageFilePath: String
) : BaseImageWidget(
        context,
        prompt,
        questionMediaManager,
        waitingForDataRegistry,
        tmpImageFilePath
    ) {
    lateinit var binding: ImageWidgetBinding

    private var selfie = false

    init {
        imageClickHandler = ViewImageClickHandler()
        imageCaptureHandler = ImageCaptureHandler()

        render()
        updateAnswer()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = ImageWidgetBinding.inflate((context as Activity).layoutInflater)

        val appearance = prompt.appearanceHint
        selfie = isFrontCameraAppearance(prompt)
        if (selfie || ((appearance != null && appearance.lowercase().contains(Appearances.NEW)))) {
            binding.chooseButton.visibility = GONE
        }

        binding.captureButton.setOnClickListener {
            getPermissionsProvider().requestCameraPermission(
                (getContext() as Activity),
                object : PermissionListener {
                    override fun granted() {
                        captureImage()
                    }
                }
            )
        }
        binding.chooseButton.setOnClickListener { v: View? ->
            imageCaptureHandler.chooseImage(
                R.string.choose_image
            )
        }
        binding.image.setOnClickListener { v: View? -> imageClickHandler.clickImage("viewImage") }

        if (questionDetails.isReadOnly) {
            binding.captureButton.visibility = GONE
            binding.chooseButton.visibility = GONE
        }

        errorTextView = binding.errorMessage
        imageView = binding.image

        return binding.root
    }

    override fun addExtrasToIntent(intent: Intent) = intent

    override fun doesSupportDefaultValues() = false

    override fun clearAnswer() {
        super.clearAnswer()
        binding.captureButton.text = context.getString(R.string.capture_image)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.captureButton.setOnLongClickListener(l)
        binding.chooseButton.setOnLongClickListener(l)
        super.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureButton.cancelLongPress()
        binding.chooseButton.cancelLongPress()
    }

    private fun captureImage() {
        if (selfie && CameraUtils().isFrontCameraAvailable(context)) {
            val intent = Intent(context, CaptureSelfieActivity::class.java).apply {
                putExtra(
                    CaptureSelfieActivity.EXTRA_TMP_PATH,
                    StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE)
                )
            }
            imageCaptureHandler.captureImage(intent, RequestCodes.MEDIA_FILE_PATH, R.string.capture_image)
        } else {
            val intent = imageCaptureIntent(formEntryPrompt, context, tmpImageFilePath)
            imageCaptureHandler.captureImage(intent, RequestCodes.IMAGE_CAPTURE, R.string.capture_image)
        }
    }
}
