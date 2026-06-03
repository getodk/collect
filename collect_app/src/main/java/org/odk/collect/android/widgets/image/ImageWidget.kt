package org.odk.collect.android.widgets.image

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.selfiecamera.CaptureSelfieActivity
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R.string
import java.util.Locale

@SuppressLint("ViewConstructor")
class ImageWidget @JvmOverloads constructor(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val tmpImageFilePath: String,
    private val dependencies: Dependencies,
    private val fileAnswerDelegate: FileAnswerDelegate = FileAnswerDelegate(questionMediaManager, questionDetails.prompt)
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    private val selfie: Boolean = Appearances.isFrontCameraAppearance(formEntryPrompt)

    init { render() }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val newImagesOnly = selfie || prompt.appearanceHint?.lowercase(Locale.ENGLISH)?.contains(Appearances.NEW) == true
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

        return ComposeView(context).apply {
            setContextThemedContent {
                ImageWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    fileAnswerDelegate.binaryName,
                    readOnly,
                    newImagesOnly,
                    buttonFontSize,
                    onCaptureClick = { captureImage() },
                    onChooseClick = { chooseImage() },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        return fileAnswerDelegate.getAnswer()
    }

    override fun clearAnswer() {
        fileAnswerDelegate.deleteFile()
        widgetValueChanged()
    }

    override fun deleteFile() {
        fileAnswerDelegate.deleteFile()
    }

    override fun setData(answer: Any) {
        if (fileAnswerDelegate.setData(answer)) {
            widgetValueChanged()
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {}

    private fun captureImage() {
        permissionsProvider.requestCameraPermission(context as Activity, object : PermissionListener {
            override fun granted() {
                if (selfie && CameraUtils().isFrontCameraAvailable(context)) {
                    val intent = Intent(context, CaptureSelfieActivity::class.java).apply {
                        putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE))
                    }
                    launchActivityForResult(intent, RequestCodes.MEDIA_FILE_PATH, string.capture_image)
                } else {
                    val intent = ImageCaptureIntentCreator.imageCaptureIntent(formEntryPrompt, context, tmpImageFilePath)
                    launchActivityForResult(intent, RequestCodes.IMAGE_CAPTURE, string.capture_image)
                }
            }
        })
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        launchActivityForResult(intent, RequestCodes.IMAGE_CHOOSER, string.choose_image)
    }

    private fun launchActivityForResult(intent: Intent, requestCode: Int, errorStringResource: Int) {
        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.index)
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(string.activity_not_found, context.getString(errorStringResource)),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
