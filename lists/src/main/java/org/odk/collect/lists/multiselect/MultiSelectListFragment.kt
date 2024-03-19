package org.odk.collect.lists.multiselect

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.lists.R
import org.odk.collect.lists.databinding.MultiSelectListBinding

class MultiSelectListFragment<T, VH : MultiSelectAdapter.ViewHolder<T>>(
    private val actionText: String,
    private val multiSelectViewModel: MultiSelectViewModel<T>,
    private val viewHolderFactory: (ViewGroup) -> VH,
    private val onViewCreated: (MultiSelectListBinding) -> Unit = {}
) : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MultiSelectControlsFragment::class) {
                MultiSelectControlsFragment(
                    actionText,
                    multiSelectViewModel
                )
            }.build()

        childFragmentManager.setFragmentResultListener(
            MultiSelectControlsFragment.REQUEST_ACTION,
            this
        ) { _, result ->
            parentFragmentManager.setFragmentResult(
                MultiSelectControlsFragment.REQUEST_ACTION,
                result
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.multi_select_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MultiSelectListBinding.bind(view)
        onViewCreated(binding)

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MultiSelectAdapter(
            multiSelectViewModel,
            viewHolderFactory
        )
        binding.list.adapter = adapter
        multiSelectViewModel.getData().observe(viewLifecycleOwner) {
            adapter.data = it
            binding.empty.isVisible = it.isEmpty()
            binding.buttons.isVisible = it.isNotEmpty()
        }
        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            adapter.selected = it
        }
    }
}
