import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.maps.layers.ReferenceLayer

class OfflineMapLayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layerTitle: TextView = itemView.findViewById(R.id.layer_name)
    val layerDetails: TextView = itemView.findViewById(R.id.layer_details)

    val expandableSection: LinearLayout = itemView.findViewById(R.id.expandable_section)
    val expandButton: ImageView = itemView.findViewById(R.id.expand_button)
    val deleteButton: Button = itemView.findViewById(R.id.delete_button)

}


class OfflineMapLayersAdapter(
        private val layers: MutableList<ReferenceLayer>,
        private val selectedLayerId: String,
        private val onSelectLayerListener: (ReferenceLayer) -> Unit,
        private val onDeleteLayerListener: (ReferenceLayer) -> Unit
) : RecyclerView.Adapter<OfflineMapLayersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineMapLayersViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.offline_map_item_layout, parent, false)
        return OfflineMapLayersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OfflineMapLayersViewHolder, position: Int) {
        val layer = layers[position]

        holder.layerTitle.text = layer.id
        holder.layerDetails.text = layer.file.path.toString()
        holder.expandButton.setOnClickListener {
            val isCurrentlyVisible = holder.expandableSection.visibility == View.VISIBLE
            holder.expandableSection.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE

            // Change the expand/collapse icon accordingly
            holder.expandButton.setImageResource(
                    if (isCurrentlyVisible) R.drawable.ic_arrow_drop_down
                    else R.drawable.ic_arrow_drop_down
            )
        }


        holder.deleteButton.setOnClickListener {
            onDeleteLayerListener.invoke(layer)
        }


        holder.itemView.setOnClickListener {
            onSelectLayerListener.invoke(layer)

        }
    }

    override fun getItemCount(): Int {
        return layers.size
    }


}


