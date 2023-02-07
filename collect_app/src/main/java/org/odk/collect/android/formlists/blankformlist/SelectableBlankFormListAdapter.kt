package org.odk.collect.android.formlists.blankformlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.databinding.SelectableBlankFormListItemBinding

class SelectableBlankFormListAdapter(private val onCheckedListener: (Long, Boolean) -> Unit) :
    RecyclerView.Adapter<SelectableBlankFormListAdapter.ViewHolder>() {

    var formItems = emptyList<BlankFormListItem>()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }
    private var isChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SelectableBlankFormListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(formItems[position]) {
                binding.formTitle.text = this.formName
                binding.checkbox.setOnCheckedChangeListener { _, checked ->
                    isChecked = checked
                    onCheckedListener(this.databaseId, checked)
                }

                binding.root.setOnClickListener {
                    binding.checkbox.toggle()
                }
            }
        }
    }

    override fun getItemCount() = formItems.size

    fun isChecked(): Boolean {
        return isChecked
    }

    class ViewHolder(val binding: SelectableBlankFormListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
