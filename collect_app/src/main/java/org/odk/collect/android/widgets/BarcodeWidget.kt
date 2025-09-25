package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.activities.ScannerWithFlashlightActivity
import org.odk.collect.android.databinding.BarcodeWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.Appearances.hasAppearance
import org.odk.collect.android.utilities.Appearances.isFrontCameraAppearance
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.CameraUtils
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class BarcodeWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val widgetAnswer: WidgetAnswer,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val cameraUtils: CameraUtils,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    lateinit var binding: BarcodeWidgetBinding

    init {
        render()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = BarcodeWidgetBinding.inflate((context as Activity).layoutInflater)

        if (prompt.isReadOnly) {
            binding.barcodeButton.visibility = GONE
        } else {
            binding.barcodeButton.setOnClickListener { onButtonClick() }
        }

        val answer = prompt.answerText
        if (!answer.isNullOrEmpty()) {
            binding.barcodeButton.text = getContext().getString(R.string.replace_barcode)
        }
        widgetAnswer.setAnswer(answer)
        binding.answerViewContainer.addView(widgetAnswer)
        updateAnswerVisibility()

        return binding.root
    }

    override fun clearAnswer() {
        widgetAnswer.setAnswer(null)
        binding.barcodeButton.text = context.getString(R.string.get_barcode)
        updateAnswerVisibility()
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        val answer = widgetAnswer.getAnswer()
        return if (answer.isEmpty()) null else StringData(answer)
    }

    override fun setData(answer: Any) {
        widgetAnswer.setAnswer(answer as String)
        binding.barcodeButton.text = context.getString(R.string.replace_barcode)
        updateAnswerVisibility()
        widgetValueChanged()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.barcodeButton.setOnLongClickListener(l)
        binding.answerViewContainer.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.barcodeButton.cancelLongPress()
        binding.answerViewContainer.cancelLongPress()
    }

    private fun updateAnswerVisibility() {
        val isAnswerHidden = hasAppearance(formEntryPrompt, Appearances.HIDDEN_ANSWER)
        binding.answerViewContainer.visibility = if (isAnswerHidden || widgetAnswer.getAnswer().isBlank()) GONE else VISIBLE
    }

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
}
