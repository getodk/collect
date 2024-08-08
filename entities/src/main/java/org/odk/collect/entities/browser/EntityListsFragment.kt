package org.odk.collect.entities.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.odk.collect.entities.R
import org.odk.collect.entities.databinding.EntityListItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding
import org.odk.collect.lists.RecyclerViewUtils

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
        binding.list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(requireContext()))

        entitiesViewModel.lists.observe(viewLifecycleOwner) {
            binding.list.adapter = ListsAdapter(it, findNavController())
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
