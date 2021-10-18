package org.odk.collect.errors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ErrorAdapter(private val errors: List<ErrorItem>) : RecyclerView.Adapter<ErrorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val secondaryText: TextView = view.findViewById(R.id.secondary_text)
        val supportingText: TextView = view.findViewById(R.id.supporting_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.error_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = errors[position].title
        holder.secondaryText.text = errors[position].secondaryText
        holder.supportingText.text = errors[position].supportingText
    }

    override fun getItemCount() = errors.size
}
