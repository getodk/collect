package org.odk.collect.errors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.errors.databinding.ErrorItemBinding

class ErrorAdapter(private val errors: List<ErrorItem>) : RecyclerView.Adapter<ErrorAdapter.ViewHolder>() {

    private lateinit var binding: ErrorItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ErrorItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(errors[position])

    override fun getItemCount() = errors.size

    class ViewHolder(val binding: ErrorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(error: ErrorItem) {
            binding.apply {
                title.text = error.title
                secondaryText.text = error.secondaryText
                supportingText.text = error.supportingText
            }
        }
    }
}
