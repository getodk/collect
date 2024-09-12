package org.odk.collect.lists.selects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.lists.R
import org.odk.collect.lists.databinding.MultiSelectListBinding

class MultiSelectListFragment<T, VH : MultiSelectAdapter.ViewHolder<T>>(
    private val actionText: String,
    private val multiSelectViewModel: MultiSelectViewModel<T>,
    private val viewHolderFactory: (ViewGroup) -> VH,
    private val onViewCreated: (MultiSelectListBinding) -> Unit = {}
) : Fragment() {

    private var appBarLayout: AppBarLayout? = null
    private lateinit var list: RecyclerView

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
        appBarLayout = requireActivity().findViewById(org.odk.collect.androidshared.R.id.appBarLayout)

        val binding = MultiSelectListBinding.bind(view)
        onViewCreated(binding)

        list = binding.list
        list.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MultiSelectAdapter(
            multiSelectViewModel,
            viewHolderFactory
        )
        list.adapter = adapter

        multiSelectViewModel.getData().observe(viewLifecycleOwner) {
            adapter.data = it
            binding.empty.isVisible = it.isEmpty()
            binding.buttons.isVisible = it.isNotEmpty()
        }
        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            adapter.selected = it
        }
    }

    override fun onResume() {
        super.onResume()
        appBarLayout?.setLiftOnScrollTargetView(list)
    }
}
