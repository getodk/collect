package org.odk.collect.androidshared.ui.multiselect

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import org.odk.collect.androidshared.databinding.MultiSelectListBinding

object MultiSelectListView {

    fun <T, VH : MultiSelectAdapter.ViewHolder<T>> setup(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        binding: MultiSelectListBinding,
        multiSelectViewModel: MultiSelectViewModel<T>,
        viewHolderFactory: (ViewGroup) -> VH
    ) {
        binding.list.layoutManager = LinearLayoutManager(context)

        val adapter = MultiSelectAdapter(multiSelectViewModel, viewHolderFactory)
        binding.list.adapter = adapter

        multiSelectViewModel.getData().observe(lifecycleOwner) {
            setData(binding, it)
        }

        multiSelectViewModel.getSelected().observe(lifecycleOwner) {
            setSelected(binding, it)
        }
    }

    private fun <T> setData(binding: MultiSelectListBinding, data: List<MultiSelectItem<T>>) {
        (binding.list.adapter as MultiSelectAdapter<T, MultiSelectAdapter.ViewHolder<T>>).data =
            data

        binding.empty.isVisible = data.isEmpty()
        binding.buttons.isVisible = data.isNotEmpty()
    }

    private fun setSelected(binding: MultiSelectListBinding, selected: Set<Long>) {
        (binding.list.adapter as MultiSelectAdapter<*, MultiSelectAdapter.ViewHolder<*>>).selected =
            selected
    }
}
