package org.odk.collect.androidshared.ui.multiselect

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import org.odk.collect.androidshared.databinding.MultiSelectControlsLayoutBinding

/**
 * A control UI for performing "select all" and "clear all" on multi select lists using
 * `MultiSelectViewModel`. Also supports an action that's text can be defined (via the
 * constructor) and can be reacted to by responding to the `"action"` Fragment result.
 */
class MultiSelectControlsFragment(
    private val actionText: String,
    private val multiSelectViewModel: MultiSelectViewModel<*>
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return MultiSelectControlsView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val controls = view as MultiSelectControlsView

        controls.actionText = actionText

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            controls.selected = it
        }

        multiSelectViewModel.isAllSelected().observe(viewLifecycleOwner) {
            controls.isAllSelected = it
        }

        controls.listener = object : MultiSelectControlsView.Listener {
            override fun onSelectAll() {
                multiSelectViewModel.selectAll()
            }

            override fun onClearAll() {
                multiSelectViewModel.unselectAll()
            }

            override fun onAction(selected: Set<Long>) {
                parentFragmentManager.setFragmentResult(
                    REQUEST_ACTION,
                    Bundle().apply {
                        putLongArray(
                            RESULT_SELECTED,
                            selected.toLongArray()
                        )
                    }
                )
            }
        }
    }

    companion object {
        const val REQUEST_ACTION = "action"
        const val RESULT_SELECTED = "selected"
    }
}

private class MultiSelectControlsView(context: Context) :
    FrameLayout(context) {

    var selected = emptySet<Long>()
        set(value) {
            field = value
            render()
        }

    var isAllSelected = false
        set(value) {
            field = value
            render()
        }

    var listener: Listener? = null
    var actionText: String = ""
        set(value) {
            field = value
            render()
        }

    private val binding =
        MultiSelectControlsLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        render()
    }

    private fun render() {
        if (isAllSelected) {
            binding.selectAll.setText(org.odk.collect.strings.R.string.clear_all)
            binding.selectAll.setOnClickListener {
                listener?.onClearAll()
            }
        } else {
            binding.selectAll.setText(org.odk.collect.strings.R.string.select_all)
            binding.selectAll.setOnClickListener {
                listener?.onSelectAll()
            }
        }

        binding.action.text = actionText
        binding.action.isEnabled = selected.isNotEmpty()
        binding.action.setOnClickListener {
            listener?.onAction(selected)
        }
    }

    interface Listener {
        fun onSelectAll()
        fun onClearAll()
        fun onAction(selected: Set<Long>)
    }
}
