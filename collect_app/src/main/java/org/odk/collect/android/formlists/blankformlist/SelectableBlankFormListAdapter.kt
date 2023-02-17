package org.odk.collect.android.formlists.blankformlist

import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R

class SelectableBlankFormListAdapter(private val onCheckedListener: (Long, Boolean) -> Unit) :
    RecyclerView.Adapter<BlankFormListItemViewHolder>() {

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
        checkbox.setOnCheckedChangeListener { _, checked ->
            onCheckedListener(item.databaseId, checked)
        }

        holder.itemView.setOnClickListener {
            checkbox.toggle()
        }
    }

    override fun getItemCount() = formItems.size
}
