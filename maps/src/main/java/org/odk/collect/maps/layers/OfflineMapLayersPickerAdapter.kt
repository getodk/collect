package org.odk.collect.maps.layers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.androidshared.ui.addOnClickListener
import org.odk.collect.maps.databinding.OfflineMapLayersPickerItemBinding
import java.io.File

class OfflineMapLayersPickerAdapter(
    private val listener: OfflineMapLayersPickerAdapterInterface
) : RecyclerView.Adapter<OfflineMapLayersPickerAdapter.ViewHolder>() {
    interface OfflineMapLayersPickerAdapterInterface {
        fun onLayerChecked(layerId: String?)
        fun onLayerToggled(layerId: String)
        fun onDeleteLayer(layerItem: CheckableReferenceLayer)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CheckableReferenceLayer>() {
        override fun areItemsTheSame(oldItem: CheckableReferenceLayer, newItem: CheckableReferenceLayer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CheckableReferenceLayer, newItem: CheckableReferenceLayer): Boolean {
            return oldItem == newItem
        }
    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OfflineMapLayersPickerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layer = asyncListDiffer.currentList[position]

        holder.binding.radioButton.setChecked(layer.isChecked)
        holder.binding.title.text = layer.name
        holder.binding.path.text = layer.file?.absolutePath
        holder.binding.arrow.isInvisible = layer.id == null

        if (layer.isExpanded) {
            holder.binding.arrow.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context, org.odk.collect.icons.R.drawable.ic_baseline_collapse_24))
            holder.binding.path.visibility = View.VISIBLE
            holder.binding.deleteLayer.visibility = View.VISIBLE
        } else {
            holder.binding.arrow.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context, org.odk.collect.icons.R.drawable.ic_baseline_expand_24))
            holder.binding.path.visibility = View.GONE
            holder.binding.deleteLayer.visibility = View.GONE
        }

        listOf(holder.binding.radioButton, holder.binding.title, holder.binding.path).addOnClickListener {
            listener.onLayerChecked(layer.id)
        }

        holder.binding.arrow.setOnClickListener {
            if (layer.id != null) {
                listener.onLayerToggled(layer.id)
            }
        }

        holder.binding.deleteLayer.setOnClickListener {
            listener.onDeleteLayer(layer)
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    fun setData(layers: List<CheckableReferenceLayer>) {
        asyncListDiffer.submitList(layers)
    }

    class ViewHolder(val binding: OfflineMapLayersPickerItemBinding) : RecyclerView.ViewHolder(binding.root)
}

data class CheckableReferenceLayer(
    val id: String?,
    val file: File?,
    val name: String,
    val isChecked: Boolean,
    val isExpanded: Boolean
)
