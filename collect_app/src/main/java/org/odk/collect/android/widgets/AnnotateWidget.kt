package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.AnnotateWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator.imageCaptureIntent
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.draw.DrawActivity
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R
import java.io.File

@SuppressLint("ViewConstructor")
class AnnotateWidget(
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
    lateinit var binding: AnnotateWidgetBinding

    init {
        imageClickHandler = DrawImageClickHandler(
            DrawActivity.OPTION_ANNOTATE,
            RequestCodes.ANNOTATE_IMAGE,
            R.string.annotate_image
        )
        imageCaptureHandler = ImageCaptureHandler()

        render()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = AnnotateWidgetBinding.inflate((context as Activity).layoutInflater)
        errorTextView = binding.errorMessage
        imageView = binding.image
        updateAnswer()

        if (formEntryPrompt.appearanceHint != null && formEntryPrompt.appearanceHint.lowercase().contains(Appearances.NEW)) {
            binding.chooseButton.visibility = GONE
        }

        if (binaryName == null || binding.image.visibility == GONE) {
            binding.annotateButton.isEnabled = false
        }

        binding.captureButton.setOnClickListener {
            getPermissionsProvider().requestCameraPermission(
                (getContext() as Activity),
                object : PermissionListener {
                    override fun granted() {
                        val intent = imageCaptureIntent(
                            formEntryPrompt, getContext(), tmpImageFilePath
                        )
                        imageCaptureHandler.captureImage(intent, RequestCodes.IMAGE_CAPTURE, R.string.annotate_image)
                    }
                }
            )
        }
        binding.chooseButton.setOnClickListener {
            imageCaptureHandler.chooseImage(
                R.string.annotate_image
            )
        }
        binding.annotateButton.setOnClickListener {
            imageClickHandler.clickImage(
                "annotateButton"
            )
        }
        binding.image.setOnClickListener { imageClickHandler.clickImage("viewImage") }

        if (questionDetails.isReadOnly) {
            binding.captureButton.visibility = GONE
            binding.chooseButton.visibility = GONE
            binding.annotateButton.visibility = GONE
        }

        return binding.root
    }

    override fun addExtrasToIntent(intent: Intent): Intent {
        var bmp: Bitmap? = null
        if (binding.image.drawable != null) {
            bmp = (binding.image.drawable as BitmapDrawable).bitmap
        }

        val screenOrientation =
            if (bmp != null && bmp.height > bmp.width) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, screenOrientation)
        return intent
    }

    override fun doesSupportDefaultValues() = true

    override fun clearAnswer() {
        super.clearAnswer()
        binding.annotateButton.isEnabled = false
        binding.captureButton.text =
            context.getString(R.string.capture_image)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.captureButton.setOnLongClickListener(l)
        binding.chooseButton.setOnLongClickListener(l)
        binding.annotateButton.setOnLongClickListener(l)
        super.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureButton.cancelLongPress()
        binding.chooseButton.cancelLongPress()
        binding.annotateButton.cancelLongPress()
    }

    override fun setData(newImageObj: Any?) {
        if (newImageObj is File) {
            val mimeType = FileUtils.getMimeType(newImageObj)
            if ("image/gif" == mimeType) {
                showLongToast(context, R.string.gif_not_supported)
            } else {
                super.setData(newImageObj)
                binding.annotateButton.isEnabled = binaryName != null
            }
        }
    }
}
