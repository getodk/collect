package org.odk.collect.android.formlists.blankformlist

import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R

class SelectableBlankFormListAdapter(private val onItemClickListener: (Long) -> Unit) :
    RecyclerView.Adapter<BlankFormListItemViewHolder>() {

    var selected: Set<Long> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var formItems = emptyList<BlankFormListItem>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlankFormListItemViewHolder {
        return BlankFormListItemViewHolder(parent).also {
            it.setEndView(R.layout.checkbox)
        }
    }

    override fun onBindViewHolder(holder: BlankFormListItemViewHolder, position: Int) {
        val item = formItems[position]
        holder.blankFormListItem = item

        val checkbox = holder.itemView.findViewById<CheckBox>(R.id.checkbox)
        checkbox.isChecked = selected.contains(item.databaseId)

        checkbox.setOnClickListener {
            onItemClickListener(item.databaseId)
        }

        holder.itemView.setOnClickListener {
            checkbox.performClick()
        }
    }

    override fun getItemCount() = formItems.size
}
