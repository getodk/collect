/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.DecimalData
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.formentry.FormEntryViewModel

class RangePickerDialogFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment() {

    private val formEntryViewModel: FormEntryViewModel by activityViewModels { viewModelFactory }
    private val prompt: FormEntryPrompt by lazy {
        formEntryViewModel.getQuestionPrompt(requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.number_picker_dialog, null)

        val numbers = requireArguments().getSerializable(ARG_VALUES) as Array<String>
        val selected = requireArguments().getInt(ARG_SELECTED)
        val decimal = requireArguments().getBoolean(ARG_DECIMAL)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker).apply {
            maxValue = numbers.size - 1
            minValue = 0
            wrapSelectorWheel = false
            displayedValues = numbers
            value = selected
        }

        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(org.odk.collect.strings.R.string.number_picker_title)
            .setView(view)
            .setPositiveButton(org.odk.collect.strings.R.string.ok) { _, _ ->
                val value = numbers[numberPicker.value]
                val answerData = if (decimal) {
                    DecimalData(value.toDouble())
                } else {
                    IntegerData(value.toInt())
                }

                formEntryViewModel.answerQuestion(prompt.index, answerData)
            }
            .setNegativeButton(org.odk.collect.strings.R.string.cancel) { _, _ -> }
            .create()
    }

    companion object {
        const val ARG_VALUES = "values"
        const val ARG_SELECTED = "selected"
        const val ARG_FORM_INDEX = "formIndex"
        const val ARG_DECIMAL = "decimal"
    }
}
