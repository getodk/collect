package org.odk.collect.errors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.errors.databinding.ErrorItemBinding

class ErrorAdapter(private val errors: List<ErrorItem>) : RecyclerView.Adapter<ErrorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ErrorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(errors[position]) {
                binding.title.text = this.title
                binding.secondaryText.text = this.secondaryText
                binding.supportingText.text = this.supportingText
            }
        }
    }

    override fun getItemCount() = errors.size

    inner class ViewHolder(val binding: ErrorItemBinding) : RecyclerView.ViewHolder(binding.root)
}
