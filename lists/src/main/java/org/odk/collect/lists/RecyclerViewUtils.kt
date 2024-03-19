package org.odk.collect.lists

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import org.odk.collect.androidshared.R

object RecyclerViewUtils {

    fun verticalLineDivider(context: Context): DividerItemDecoration {
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = ContextCompat.getDrawable(context, R.drawable.list_item_divider)!!
        itemDecoration.setDrawable(drawable)

        return itemDecoration
    }
}
