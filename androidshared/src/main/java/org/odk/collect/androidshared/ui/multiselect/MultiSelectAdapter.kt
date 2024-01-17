package org.odk.collect.androidshared.ui.multiselect

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class MultiSelectAdapter<T>(
    private val multiSelectViewModel: MultiSelectViewModel,
    private val viewHolderFactory: (ViewGroup) -> ViewHolder<T>
) : RecyclerView.Adapter<MultiSelectAdapter.ViewHolder<T>>() {

    var selected: Set<Long> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var data = emptyList<T>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return viewHolderFactory(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = data[position]
        holder.setItem(item)

        val checkbox = holder.getCheckbox().also {
            it.isChecked = selected.contains(holder.getId())
            it.setOnClickListener {
                multiSelectViewModel.toggle(holder.getId())
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
        abstract fun getId(): Long
        abstract fun getCheckbox(): CheckBox
    }
}
