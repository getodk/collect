package org.odk.collect.android.formlists.blankformlist

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BlankFormListItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    BlankFormListItemView(parent.context)
) {
    var blankFormListItem: BlankFormListItem? = null
        set(value) {
            field = value
            (itemView as BlankFormListItemView).blankFormListItem = field
        }

    init {
        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun setTrailingView(layoutId: Int) {
        (itemView as BlankFormListItemView).setTrailingView(layoutId)
    }
}
