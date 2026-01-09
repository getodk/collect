package org.odk.collect.android.widgets.utilities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.databinding.WidgetAnswerDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.material.MaterialFullScreenDialogFragment
import kotlin.reflect.KClass

abstract class WidgetAnswerDialogFragment<T : Fragment>(
    private val type: KClass<T>,
    private val viewModelFactory: ViewModelProvider.Factory
) : MaterialFullScreenDialogFragment() {

    private val formEntryViewModel: FormEntryViewModel by activityViewModels { viewModelFactory }
    private val prompt: FormEntryPrompt by lazy {
        formEntryViewModel.getQuestionPrompt(requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex)
    }
    protected val validationResult by lazy {
        formEntryViewModel.validationResult
    }

    abstract fun onCreateFragment(prompt: FormEntryPrompt): T

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(type) { onCreateFragment(prompt) }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = WidgetAnswerDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (childFragmentManager.fragments.isEmpty()) {
            childFragmentManager.commit {
                add(R.id.answer_fragment, type.java, null)
            }
        }
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onCloseClicked() {
        // No toolbar so not relevant
    }

    fun onValidate(answer: IAnswerData?) {
        formEntryViewModel.validateAnswer(prompt.index, answer)
    }

    fun onAnswer(answer: IAnswerData?) {
        formEntryViewModel.answerQuestion(prompt.index, answer)
        dismiss()
    }

    companion object {
        const val ARG_FORM_INDEX = "form_index"
    }
}
