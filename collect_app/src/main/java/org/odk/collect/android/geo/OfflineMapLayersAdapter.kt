package org.odk.collect.android.geo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.maps.layers.ReferenceLayer

class OfflineMapLayersAdapter(
        private val layers: MutableList<ReferenceLayer>, // Layers, including the 'None' option
        private var referenceLayerId: String, // ID of the initially selected layer, "" for 'None'
        private val onSelectLayerListener: (ReferenceLayer) -> Unit,
        private val onDeleteLayerListener: (ReferenceLayer) -> Unit
) : RecyclerView.Adapter<OfflineMapLayersAdapter.OfflineMapLayersViewHolder>() {

    // Position of the 'None' option is always 0
    private var selectedPosition: Int = layers.indexOfFirst { it.id == referenceLayerId }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineMapLayersViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.offline_map_item_layout, parent, false)
        return OfflineMapLayersViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OfflineMapLayersViewHolder, position: Int) {
        val layer = layers[position]

        // Setup views based on whether it's a 'None' option or a regular layer
        if (layer.id == "none") {
            holder.layerTitle.text = "None"
            holder.layerDetails.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            holder.expandButton.visibility = View.GONE
        } else {
            holder.layerTitle.text = layer.id // Adjust based on your ReferenceLayer properties
            holder.layerDetails.text = layer.file.path
            holder.layerDetails.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            holder.expandButton.visibility = View.VISIBLE

            // Set the initial state of the expandable section and button
            holder.expandableSection.visibility = View.GONE
            holder.expandButton.setImageResource(R.drawable.ic_arrow_drop_down)

            // Toggle the visibility of the expandable section when the expand button is clicked
            holder.expandButton.setOnClickListener {
                val isCurrentlyVisible = holder.expandableSection.visibility == View.VISIBLE
                holder.expandableSection.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
                holder.expandButton.animate().rotation(if (isCurrentlyVisible) 0f else 180f).start()
            }
        }

        holder.itemView.findViewById<RadioButton>(R.id.radio_button).apply {
            isClickable = false
            isFocusable = false
        }

        holder.deleteButton.setOnClickListener {
            onDeleteLayerListener.invoke(layer)
        }

        // Set the radio button's checked state based on the current selection
        holder.itemView.findViewById<RadioButton>(R.id.radio_button).isChecked = (position == selectedPosition)

        holder.itemView.setOnClickListener {
            val latestPosition = holder.adapterPosition
            if (latestPosition != RecyclerView.NO_POSITION && selectedPosition != latestPosition) {
                val previousSelectedPosition = selectedPosition
                selectedPosition = latestPosition
                referenceLayerId = layer.id
                notifyItemChanged(previousSelectedPosition) // Refresh previous selection
                notifyItemChanged(selectedPosition) // Refresh new selection
                onSelectLayerListener(layer) // Notify listener
            }
        }
    }

    override fun getItemCount(): Int = layers.size

    class OfflineMapLayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layerTitle: TextView = itemView.findViewById(R.id.layer_name)
        val layerDetails: TextView = itemView.findViewById(R.id.layer_details)
        val expandableSection: LinearLayout = itemView.findViewById(R.id.expandable_section)
        val expandButton: ImageView = itemView.findViewById(R.id.expand_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }
}
