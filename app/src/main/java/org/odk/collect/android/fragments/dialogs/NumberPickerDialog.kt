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
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R

class NumberPickerDialog : DialogFragment() {
    interface NumberPickerListener {
        fun onNumberPickerValueSelected(widgetId: Int, value: Int)
    }

    private var listener: NumberPickerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NumberPickerListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.number_picker_dialog, null)

        val numbers = requireArguments().getSerializable(DISPLAYED_VALUES) as Array<String>

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker).apply {
            maxValue = numbers.size - 1
            minValue = 0
            wrapSelectorWheel = false
            displayedValues = numbers
            value = requireArguments().getInt(PROGRESS)
        }

        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.number_picker_title)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                listener?.onNumberPickerValueSelected(
                    requireArguments().getInt(WIDGET_ID), numberPicker.value
                )
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }

    companion object {
        const val NUMBER_PICKER_DIALOG_TAG = "numberPickerDialogTag"
        const val WIDGET_ID = "widgetId"
        const val DISPLAYED_VALUES = "displayedValues"
        const val PROGRESS = "progress"

        @JvmStatic
        fun newInstance(
            widgetId: Int,
            displayedValues: Array<String>,
            progress: Int
        ): NumberPickerDialog {
            return NumberPickerDialog().apply {
                arguments = Bundle().apply {
                    putInt(WIDGET_ID, widgetId)
                    putInt(PROGRESS, progress)
                    putSerializable(DISPLAYED_VALUES, displayedValues)
                }
            }
        }
    }
}
