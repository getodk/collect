package org.odk.collect.android.formlists.sorting

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.SortItemLayoutBinding
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import java.util.function.Consumer

class FormListSortingAdapter(
    private val sortingOptions: List<FormListSortingOption>,
    private val selectedSortingOrder: Int,
    private val listener: Consumer<Int>
) : RecyclerView.Adapter<FormListSortingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SortItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            listener.accept(position)
            selectItem(holder.binding)
        }

        val sortingOption = sortingOptions[position]

        holder.binding.title.setText(sortingOption.text)
        holder.binding.icon.setImageResource(sortingOption.icon)
        holder.binding.icon.tag = sortingOption.icon
        holder.binding.icon.setImageDrawable(
            DrawableCompat.wrap(holder.binding.icon.drawable).mutate()
        )

        if (position == selectedSortingOrder) {
            selectItem(holder.binding)
        }
    }

    private fun selectItem(binding: SortItemLayoutBinding) {
        binding.title.setTextColor(getThemeAttributeValue(binding.root.context, R.attr.colorAccent))
        DrawableCompat.setTintList(
            binding.icon.drawable,
            ColorStateList.valueOf(getThemeAttributeValue(binding.root.context, R.attr.colorAccent))
        )
    }

    override fun getItemCount() = sortingOptions.size

    class ViewHolder(val binding: SortItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
