package org.odk.collect.lists.multiselect.support

import android.content.Context
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.lists.multiselect.MultiSelectAdapter

class TextAndCheckBoxView(context: Context) : FrameLayout(context) {

    val textView = TextView(context).also {
        it.id = TEXT_VIEW_ID
        addView(it)
    }

    val checkBox = CheckBox(context).also {
        it.id = CHECK_BOX_ID
        addView(it)
    }

    companion object {
        const val TEXT_VIEW_ID = 101
        const val CHECK_BOX_ID = 102
    }
}

class TextAndCheckBoxViewHolder<T>(context: Context) :
    MultiSelectAdapter.ViewHolder<T>(TextAndCheckBoxView(context)) {

    val view = itemView as TextAndCheckBoxView

    override fun setItem(item: T) {
        view.textView.text = item.toString()
    }

    override fun getCheckbox(): CheckBox {
        return view.checkBox
    }
}
