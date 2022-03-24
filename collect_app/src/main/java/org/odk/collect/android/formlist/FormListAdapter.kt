package org.odk.collect.android.formlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.databinding.FormListItemBinding

class FormListAdapter(private val formItems: List<FormListItem>) : RecyclerView.Adapter<FormListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FormListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount() = formItems.size

    class ViewHolder(binding: FormListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
