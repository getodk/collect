package org.odk.collect.androidshared.ui.multiselect

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class MultiSelectAdapter<T, VH : MultiSelectAdapter.ViewHolder<T>>(
    private val multiSelectViewModel: MultiSelectViewModel<*>,
    private val viewHolderFactory: (ViewGroup) -> VH
) : RecyclerView.Adapter<VH>() {

    var selected: Set<Long> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var data = emptyList<MultiSelectItem<T>>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return viewHolderFactory(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.setItem(item.item)

        val checkbox = holder.getCheckbox().also {
            it.isChecked = selected.contains(item.id)
            it.setOnClickListener {
                multiSelectViewModel.toggle(item.id)
            }
        }

        holder.itemView.setOnClickListener {
            checkbox.performClick()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    abstract class ViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun setItem(item: T)
        abstract fun getCheckbox(): CheckBox
    }
}
