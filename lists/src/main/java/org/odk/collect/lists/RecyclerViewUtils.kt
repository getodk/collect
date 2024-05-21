package org.odk.collect.lists

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.androidshared.R

object RecyclerViewUtils {

    fun verticalLineDivider(context: Context): DividerItemDecoration {
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = ContextCompat.getDrawable(context, R.drawable.list_item_divider)!!
        itemDecoration.setDrawable(drawable)

        return itemDecoration
    }

    fun RecyclerView.ViewHolder.matchParentWidth() {
        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
