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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.entities.databinding.AddEntitiesDialogLayoutBinding
import org.odk.collect.entities.databinding.EntityListItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding

class EntityListsFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: () -> MenuHost
) : Fragment() {

    private val entitiesViewModel by viewModels<EntitiesViewModel> { viewModelFactory }

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

        entitiesViewModel.lists.observe(viewLifecycleOwner) {
            binding.list.adapter = ListsAdapter(it, findNavController())
        }

        menuHost().addMenuProvider(
            ListsMenuProvider(entitiesViewModel, requireContext()),
            viewLifecycleOwner
        )
    }
}

private class ListsMenuProvider(
    private val entitiesViewModel: EntitiesViewModel,
    private val context: Context
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.entity_lists, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.clear_entities -> {
                entitiesViewModel.clearAll()
                true
            }

            R.id.add_entity_list -> {
                val binding = AddEntitiesDialogLayoutBinding.inflate(LayoutInflater.from(context))
                MaterialAlertDialogBuilder(context)
                    .setView(binding.root)
                    .setPositiveButton(org.odk.collect.strings.R.string.add) { _, _ ->
                        entitiesViewModel.addEntityList(binding.entityListName.text.toString())
                    }
                    .show()
                true
            }

            else -> false
        }
    }
}

private class ListsAdapter(
    private val data: List<String>,
    private val navController: NavController
) : RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ListViewHolder {
        return ListViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: ListViewHolder, position: Int) {
        val list = data[position]
        viewHolder.setList(list)
        viewHolder.itemView.setOnClickListener {
            navController.navigate(
                R.id.lists_to_entities,
                EntitiesFragmentArgs(list).toBundle()
            )
        }
    }
}

private class ListViewHolder(parent: ViewGroup) : ViewHolder(
    EntityListItemLayoutBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ).root
) {

    fun setList(list: String) {
        EntityListItemLayoutBinding.bind(itemView).name.text = list
    }
}
