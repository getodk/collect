package org.odk.collect.android.formlists.blankformlist

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class BlankFormListAdapter(
    val listener: OnFormItemClickListener
) : RecyclerView.Adapter<BlankFormListItemViewHolder>() {

    private var formItems = emptyList<BlankFormListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlankFormListItemViewHolder {
        return BlankFormListItemViewHolder(parent).also {
            it.setTrailingView(R.layout.map_button)
        }
    }

    override fun onBindViewHolder(holder: BlankFormListItemViewHolder, position: Int) {
        val item = formItems[position]
        holder.blankFormListItem = item

        holder.itemView.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                listener.onFormClick(item.contentUri)
            }
        }

        val mapButton = holder.itemView.findViewById<Button>(R.id.map_button)

        mapButton.visibility = if (item.geometryPath.isNotBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        mapButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                listener.onMapButtonClick(item.databaseId)
            }
        }
    }

    override fun getItemCount() = formItems.size

    fun setData(blankFormItems: List<BlankFormListItem>) {
        this.formItems = blankFormItems.toList()
        notifyDataSetChanged()
    }
}

interface OnFormItemClickListener {
    fun onFormClick(formUri: Uri)

    fun onMapButtonClick(id: Long)
}
