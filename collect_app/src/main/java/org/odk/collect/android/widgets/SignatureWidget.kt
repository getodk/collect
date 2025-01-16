package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.SignatureWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.draw.DrawActivity
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class SignatureWidget(
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
    lateinit var binding: SignatureWidgetBinding

    init {
        imageClickHandler = DrawImageClickHandler(
            DrawActivity.OPTION_SIGNATURE,
            RequestCodes.SIGNATURE_CAPTURE,
            R.string.signature_capture
        )

        render()
        updateAnswer()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = SignatureWidgetBinding.inflate((context as Activity).layoutInflater)
        binding.signButton.setOnClickListener {
            imageClickHandler.clickImage(
                "signButton"
            )
        }
        binding.image.setOnClickListener { imageClickHandler.clickImage("viewImage") }

        if (questionDetails.isReadOnly) {
            binding.signButton.visibility = GONE
        }

        errorTextView = binding.errorMessage
        imageView = binding.image

        return binding.root
    }

    override fun addExtrasToIntent(intent: Intent) = intent

    override fun doesSupportDefaultValues() = true

    override fun clearAnswer() {
        super.clearAnswer()
        binding.signButton.text = context.getString(R.string.sign_button)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.signButton.setOnLongClickListener(l)
        super.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.signButton.cancelLongPress()
    }
}
