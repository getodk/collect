package org.odk.collect.android.widgets.video

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.android.widgets.WidgetAnswerViewModelProvider
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
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
    private var binaryName by mutableStateOf<String?>(formEntryPrompt.getAnswerText())

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val newVideoOnly = formEntryPrompt.appearanceHint?.lowercase()?.contains(Appearances.NEW) ?: false
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

        return ComposeView(context).apply {
            setContextThemedContent {
                VideoWidgetContent(
                    readOnly,
                    newVideoOnly,
                    buttonFontSize,
                    onRecordClick = {
                        getPermissionsProvider().requestCameraPermission(
                            context as Activity,
                            object : PermissionListener {
                                override fun granted() {
                                    captureVideo()
                                }
                            }
                        )
                    },
                    onChooseClick = { chooseVideo() },
                    onLongClick = { this.showContextMenu() }
                ) {
                    WidgetAnswer(
                        Modifier.padding(top = dimensionResource(id = dimen.margin_standard)),
                        formEntryPrompt,
                        binaryName,
                        WidgetAnswerViewModelProvider(context as ComponentActivity, scheduler, questionMediaManager, mediaUtils)
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

        if (answer is File) {
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
            Timber.e(Error("VideoWidget's setBinaryData must receive a File or Uri object."))
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

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
            ).show()
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
            ).show()

            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
