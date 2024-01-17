package org.odk.collect.androidshared.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.androidshared.databinding.MultiSelectControlsLayoutBinding

class MultiSelectControlsView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

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
