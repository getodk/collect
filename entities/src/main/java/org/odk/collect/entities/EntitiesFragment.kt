package org.odk.collect.entities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        savedInstanceState: Bundle?,
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dataset = EntitiesFragmentArgs.fromBundle(requireArguments()).dataset
        val binding = ListLayoutBinding.bind(view)

        entitiesRepository.getEntities(dataset).forEach { entity ->
            val item = EntityItemView(view.context)
            item.setEntity(entity)

            binding.list.addView(item)
        }
    }
}
