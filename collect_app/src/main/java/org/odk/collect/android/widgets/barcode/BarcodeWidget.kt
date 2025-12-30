package org.odk.collect.android.widgets.barcode

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.activities.ScannerWithFlashlightActivity
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.Appearances.hasAppearance
import org.odk.collect.android.utilities.Appearances.isFrontCameraAppearance
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class BarcodeWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val dependencies: Dependencies,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val cameraUtils: CameraUtils
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    private var answer by mutableStateOf<String?>(formEntryPrompt.answerText)

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            val readOnly = questionDetails.isReadOnly
            val isAnswerHidden = hasAppearance(formEntryPrompt, Appearances.HIDDEN_ANSWER)
            val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)
            val viewModelProvider = ViewModelProvider(
                context as ComponentActivity,
                dependencies.viewModelFactory
            )

            setContextThemedContent {
                BarcodeWidgetContent(
                    viewModelProvider,
                    formEntryPrompt,
                    answer,
                    readOnly,
                    isAnswerHidden,
                    buttonFontSize,
                    answerFontSize,
                    onGetBarcodeClick = { onButtonClick() },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun clearAnswer() {
        answer = null
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return if (answer.isNullOrEmpty()) null else StringData(answer!!)
    }

    override fun setData(answer: Any) {
        this.answer = stripInvalidCharacters(answer as String)
        widgetValueChanged()
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    private fun onButtonClick() {
        getPermissionsProvider().requestCameraPermission(
            (context as Activity),
            object : PermissionListener {
                override fun granted() {
                    waitingForDataRegistry.waitForData(formEntryPrompt.index)
                    val intent = IntentIntegrator(context as Activity)
                        .setCaptureActivity(ScannerWithFlashlightActivity::class.java)
                    setCameraIdIfNeeded(formEntryPrompt, intent)
                    intent.initiateScan()
                }
            }
        )
    }

    private fun setCameraIdIfNeeded(prompt: FormEntryPrompt, intent: IntentIntegrator) {
        if (isFrontCameraAppearance(prompt)) {
            if (cameraUtils.isFrontCameraAvailable(context)) {
                intent.addExtra(Appearances.FRONT, true)
            } else {
                showLongToast(R.string.error_front_camera_unavailable)
            }
        }
    }

    // Remove control characters, invisible characters and unused code points.
    private fun stripInvalidCharacters(data: String?): String? {
        return data?.replace("\\p{C}".toRegex(), "")
    }
}
