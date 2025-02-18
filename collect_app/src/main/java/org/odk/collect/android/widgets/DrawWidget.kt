package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.DrawWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.draw.DrawActivity
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class DrawWidget(
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
    lateinit var binding: DrawWidgetBinding

    init {
        imageClickHandler = DrawImageClickHandler(
            DrawActivity.OPTION_DRAW,
            RequestCodes.DRAW_IMAGE,
            R.string.draw_image
        )

        render()
        updateAnswer()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = DrawWidgetBinding.inflate((context as Activity).layoutInflater)
        binding.drawButton.setOnClickListener {
            imageClickHandler.clickImage(
                "drawButton"
            )
        }
        binding.answerView.setOnClickListener { imageClickHandler.clickImage("viewImage") }

        if (questionDetails.isReadOnly) {
            binding.drawButton.visibility = GONE
        }

        answerView = binding.answerView
        answerView.setup(prompt.answerValue, imageLoader, questionMediaManager, referenceManager, getDefaultFilePath())
        return binding.root
    }

    override fun addExtrasToIntent(intent: Intent) = intent

    override fun clearAnswer() {
        super.clearAnswer()
        binding.drawButton.text = context.getString(R.string.draw_image)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.drawButton.setOnLongClickListener(l)
        super.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.drawButton.cancelLongPress()
    }
}
