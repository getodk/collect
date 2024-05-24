package org.odk.collect.maps.layers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.maps.databinding.OfflineMapLayersImportItemBinding

class OfflineMapLayersImportAdapter(
    private val layers: List<ReferenceLayer>,
) : RecyclerView.Adapter<OfflineMapLayersImportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OfflineMapLayersImportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.layerName.text = layers[position].name
    }

    override fun getItemCount() = layers.size

    class ViewHolder(val binding: OfflineMapLayersImportItemBinding) : RecyclerView.ViewHolder(binding.root)
}
