import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R

// Example data class representing an Offline Map Layer


// Example ViewHolder for the adapter
class OfflineMapLayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layerTitle: TextView = itemView.findViewById(R.id.layerTitle)
    val layerIcon: ImageView = itemView.findViewById(R.id.layerIcon)
    // Other views in your layout
}

// Example Adapter for Offline Map Layers
class OfflineMapLayersAdapter(
        private val layers: List<OfflineMapLayer>,
        private val selectedLayer: Int,
        private val listener: (Int) -> Unit // Lambda listener to handle item clicks
) : RecyclerView.Adapter<OfflineMapLayersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineMapLayersViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.offline_map_item_layout, parent, false)
        return OfflineMapLayersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OfflineMapLayersViewHolder, position: Int) {
        val layer = layers[position]

        holder.layerTitle.text = layer.title
        holder.layerIcon.setImageResource(layer.icon)
        holder.layerIcon.tag = layer.icon

        // Apply color to selected item
        if (position == selectedLayer) {
            val colorAccent = ContextCompat.getColor(holder.itemView.context, R.color.colorOnPrimary)

            holder.layerTitle.setTextColor(colorAccent)
            DrawableCompat.setTintList(
                    holder.layerIcon.drawable,
                    ColorStateList.valueOf(colorAccent)
            )
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            listener.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return layers.size
    }
}
