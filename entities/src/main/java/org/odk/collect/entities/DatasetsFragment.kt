package org.odk.collect.entities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.entities.databinding.DatasetItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding

class DatasetsFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: () -> MenuHost
) : Fragment() {

    private val entitiesViewModel by viewModels<EntitiesViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(AddEntityListDialogFragment::class) {
                AddEntityListDialogFragment(entitiesViewModel)
            }.build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ListLayoutBinding.bind(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext())

        entitiesViewModel.datasets.observe(viewLifecycleOwner) {
            binding.list.adapter = DatasetsAdapter(it, findNavController())
        }

        menuHost().addMenuProvider(
            DatasetsMenuProvider(entitiesViewModel, childFragmentManager),
            viewLifecycleOwner
        )
    }
}

private class DatasetsMenuProvider(
    private val entitiesViewModel: EntitiesViewModel,
    private val childFragmentManager: FragmentManager
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.datasets, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.clear_entities -> {
                entitiesViewModel.clearAll()
                true
            }

            R.id.add_entity_list -> {
                childFragmentManager.showIfNotShowing(AddEntityListDialogFragment::class)
                true
            }

            else -> false
        }
    }
}

private class DatasetsAdapter(
    private val data: List<String>,
    private val navController: NavController
) : RecyclerView.Adapter<DatasetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DatasetViewHolder {
        return DatasetViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: DatasetViewHolder, position: Int) {
        val dataset = data[position]
        viewHolder.setDataset(dataset)
        viewHolder.itemView.setOnClickListener {
            navController.navigate(
                R.id.datasets_to_entities,
                EntitiesFragmentArgs(dataset).toBundle()
            )
        }
    }
}

private class DatasetViewHolder(parent: ViewGroup) : ViewHolder(
    DatasetItemLayoutBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ).root
) {

    fun setDataset(dataset: String) {
        DatasetItemLayoutBinding.bind(itemView).name.text = dataset
    }
}
