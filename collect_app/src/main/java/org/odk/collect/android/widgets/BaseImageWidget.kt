package org.odk.collect.android.widgets

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.IdRes
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.InvalidReferenceException
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.draw.DrawActivity
import org.odk.collect.strings.R
import timber.log.Timber
import java.io.File

abstract class BaseImageWidget(
    context: Context,
    prompt: QuestionDetails,
    protected val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    protected val tmpImageFilePath: String
) : QuestionWidget(context, prompt), FileWidget, WidgetDataReceiver {
    lateinit var answerView: ImageWidgetAnswer
    protected lateinit var imageClickHandler: ImageClickHandler
    protected lateinit var imageCaptureHandler: ExternalImageCaptureHandler
    protected var binaryName: String?

    init {
        binaryName = formEntryPrompt.answerText
    }

    override fun getAnswer(): IAnswerData? {
        return if (binaryName == null) null else StringData(binaryName!!)
    }

    override fun clearAnswer() {
        deleteFile()
        answerView.setAnswer(null)
        widgetValueChanged()
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(formEntryPrompt.index.toString(), binaryName)
        binaryName = null
    }

    override fun setData(newImage: Any?) {
        if (binaryName != null) {
            deleteFile()
        }

        if (newImage is File) {
            if (newImage.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPrompt.index.toString(),
                    newImage.absolutePath
                )
                binaryName = newImage.name
                updateAnswer()
                widgetValueChanged()
            } else {
                Timber.e(Error("NO IMAGE EXISTS at: " + newImage.absolutePath))
            }
        } else {
            Timber.e(Error("ImageWidget's setBinaryData must receive a File object."))
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        answerView.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        answerView.cancelLongPress()
    }

    protected fun updateAnswer() {
        answerView.setAnswer(binaryName)
    }

    /**
     * Enables a subclass to add extras to the intent before launching the draw activity.
     *
     * @param intent to add extras
     * @return intent with added extras
     */
    abstract fun addExtrasToIntent(intent: Intent): Intent

    /**
     * Interface for Clicking on Images
     */
    protected interface ImageClickHandler {
        fun clickImage(context: String)
    }

    /**
     * Class to implement launching of viewing an image Activity
     */
    protected inner class ViewImageClickHandler : ImageClickHandler {
        override fun clickImage(context: String) {
            mediaUtils.openFile(
                getContext(), questionMediaManager.getAnswerFile(binaryName)!!,
                "image/*"
            )
        }
    }

    /**
     * Class to implement launching of drawing image Activity when clicked
     */
    protected inner class DrawImageClickHandler(
        private val drawOption: String,
        private val requestCode: Int,
        private val stringResourceId: Int
    ) : ImageClickHandler {
        override fun clickImage(context: String) {
            if (allowClick(javaClass.name)) {
                launchDrawActivity()
            }
        }

        private fun launchDrawActivity() {
            var i = Intent(context, DrawActivity::class.java)
            i.putExtra(DrawActivity.OPTION, drawOption)
            val file = answerView.getFile(binaryName)
            if (file != null && file.exists()) {
                i.putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(file))
            }

            i.putExtra(
                DrawActivity.EXTRA_OUTPUT, Uri.fromFile(
                    File(
                        tmpImageFilePath
                    )
                )
            )
            i = addExtrasToIntent(i)
            launchActivityForResult(i, requestCode, stringResourceId)
        }
    }

    /**
     * Interface for choosing or capturing a new image
     */
    protected interface ExternalImageCaptureHandler {
        fun captureImage(intent: Intent?, requestCode: Int, stringResource: Int)

        fun chooseImage(stringResource: Int)
    }

    /**
     * Class for launching the image capture or choose image activities
     */
    protected inner class ImageCaptureHandler : ExternalImageCaptureHandler {
        override fun captureImage(intent: Intent?, requestCode: Int, stringResource: Int) {
            launchActivityForResult(intent, requestCode, stringResource)
        }

        override fun chooseImage(@IdRes stringResource: Int) {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.setType("image/*")
            launchActivityForResult(
                i,
                ApplicationConstants.RequestCodes.IMAGE_CHOOSER,
                stringResource
            )
        }
    }

    /**
     * Standard method for launching an Activity.
     *
     * @param intent              - The Intent to start
     * @param resourceCode        - Code to return when Activity exits
     * @param errorStringResource - String resource for error toast
     */
    protected fun launchActivityForResult(
        intent: Intent?,
        resourceCode: Int,
        errorStringResource: Int
    ) {
        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.index)
            (context as Activity).startActivityForResult(intent, resourceCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(
                    R.string.activity_not_found,
                    context.getString(errorStringResource)
                ),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }

    protected fun getDefaultFilePath(): String? {
        try {
            return referenceManager.deriveReference(binaryName).localURI
        } catch (e: InvalidReferenceException) {
            Timber.w(e)
        }

        return null
    }
}
