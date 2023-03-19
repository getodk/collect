package org.odk.collect.android.formlists

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R

class FormListRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        layoutManager = LinearLayoutManager(context)
        val itemDecoration = DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_item_divider)!!)
        addItemDecoration(itemDecoration)
    }
}
