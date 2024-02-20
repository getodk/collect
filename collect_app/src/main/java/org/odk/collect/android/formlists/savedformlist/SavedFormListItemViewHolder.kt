package org.odk.collect.android.formlists.savedformlist

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiselect.MultiSelectAdapter
import org.odk.collect.forms.instances.Instance

class SavedFormListItemViewHolder(parent: ViewGroup) : MultiSelectAdapter.ViewHolder<Instance>(
    SavedFormListItemView(parent.context)
) {
    private var selectView = itemView

    init {
        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun setItem(item: Instance) {
        (itemView as SavedFormListItemView).instance = item
    }

    override fun getCheckbox(): CheckBox {
        return (itemView as SavedFormListItemView).binding.checkbox
    }

    override fun getSelectArea(): View {
        return selectView
    }

    fun setOnDetailsClickListener(listener: () -> Unit) {
        selectView = itemView.findViewById(R.id.selectView)
        selectView.setOnClickListener { listener() }
    }
}
