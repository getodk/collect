package org.odk.collect.android.formlists.blankformlist

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.lists.RecyclerViewUtils.setItemViewLayoutParams

class BlankFormListAdapter(
    val listener: OnFormItemClickListener
) : RecyclerView.Adapter<BlankFormListAdapter.BlankFormListItemWithMapViewHolder>() {

    private var formItems = emptyList<BlankFormListItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlankFormListItemWithMapViewHolder {
        return BlankFormListItemWithMapViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BlankFormListItemWithMapViewHolder, position: Int) {
        val item = formItems[position]
        holder.setItem(item)

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

    class BlankFormListItemWithMapViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        BlankFormListItemView(parent.context).also {
            it.setTrailingView(R.layout.map_button)
        }
    ) {
        fun setItem(item: BlankFormListItem) {
            (itemView as BlankFormListItemView).setItem(item)
        }

        init {
            setItemViewLayoutParams()
        }
    }
}

interface OnFormItemClickListener {
    fun onFormClick(formUri: Uri)

    fun onMapButtonClick(id: Long)
}
