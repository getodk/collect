package org.odk.collect.entities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.odk.collect.entities.databinding.ListItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding
import javax.inject.Inject

class DatasetsFragment : Fragment() {

    @Inject
    lateinit var entitiesRepository: EntitiesRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as EntitiesDependencyComponentProvider).entitiesDependencyComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ListLayoutBinding.bind(view)

        entitiesRepository.getDatasets().forEach { dataset ->
            val item = ListItemLayoutBinding.inflate(layoutInflater)
            item.content.text = dataset

            item.root.setOnClickListener {
                view.findNavController().navigate(R.id.datasets_to_entities, EntitiesFragmentArgs(dataset).toBundle())
            }

            binding.list.addView(item.root)
        }
    }
}
