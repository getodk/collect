package org.odk.collect.entities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.odk.collect.entities.databinding.DatasetItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding
import javax.inject.Inject

class DatasetsFragment : Fragment() {

    @Inject
    lateinit var entitiesRepository: EntitiesRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as EntitiesDependencyComponentProvider).entitiesDependencyComponent.inject(
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val datasets = entitiesRepository.getDatasets().toList()

        val binding = ListLayoutBinding.bind(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = DatasetsAdapter(datasets, findNavController())
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
