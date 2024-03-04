package org.odk.collect.entities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.odk.collect.entities.databinding.ListLayoutBinding
import javax.inject.Inject

class EntitiesFragment : Fragment() {

    @Inject
    lateinit var entitiesRepository: EntitiesRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as EntitiesDependencyComponentProvider)
            .entitiesDependencyComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dataset = EntitiesFragmentArgs.fromBundle(requireArguments()).dataset
        val entities = entitiesRepository.getEntities(dataset)

        val binding = ListLayoutBinding.bind(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = EntitiesAdapter(entities)
    }
}

private class EntitiesAdapter(private val data: List<Entity>) :
    RecyclerView.Adapter<EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): EntityViewHolder {
        return EntityViewHolder(parent.context)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: EntityViewHolder, position: Int) {
        val entity = data[position]
        viewHolder.setEntity(entity)
    }
}

private class EntityViewHolder(context: Context) : ViewHolder(EntityItemView(context)) {

    fun setEntity(entity: Entity) {
        (itemView as EntityItemView).setEntity(entity)
    }
}
