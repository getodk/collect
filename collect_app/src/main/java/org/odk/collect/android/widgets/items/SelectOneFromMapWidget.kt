package org.odk.collect.android.widgets.items

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.SelectOneFromMapWidgetAnswerBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.androidshared.ui.DialogFragmentUtils

@SuppressLint("ViewConstructor")
class SelectOneFromMapWidget(context: Context, questionDetails: QuestionDetails) :
    QuestionWidget(context, questionDetails) {

    lateinit var binding: SelectOneFromMapWidgetAnswerBinding

    override fun onCreateAnswerView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        binding = SelectOneFromMapWidgetAnswerBinding.inflate(LayoutInflater.from(context))

        binding.button.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                SelectOneFromMapDialogFragment::class.java,
                Bundle().also { it.putSerializable(ARG_FORM_INDEX, prompt.index) },
                (context as FragmentActivity).supportFragmentManager
            )
        }

        return binding.root
    }

    override fun getAnswer(): IAnswerData? {
        return null
    }

    override fun clearAnswer() {}

    override fun setOnLongClickListener(l: OnLongClickListener?) {}
}
