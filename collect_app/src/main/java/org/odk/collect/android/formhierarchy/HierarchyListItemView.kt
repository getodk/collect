package org.odk.collect.android.formhierarchy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.android.utilities.HtmlUtils

class HierarchyListItemView(context: Context, viewType: Int) : FrameLayout(context) {
    init {
        when (viewType) {
            HierarchyItemType.QUESTION.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_question_item, this, true)
            HierarchyItemType.VISIBLE_GROUP.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_group_item, this, true)
            HierarchyItemType.REPEATABLE_GROUP.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_item, this, true)
            HierarchyItemType.REPEAT_INSTANCE.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_instance_item, this, true)
        }
    }

    fun setElement(item: HierarchyItem) {
        findViewById<MaterialTextView>(R.id.primary_text).text = item.primaryText
        if (item.hierarchyItemType == HierarchyItemType.QUESTION) {
            if (item.secondaryText.isNullOrBlank()) {
                findViewById<MaterialTextView>(R.id.secondary_text).visibility = View.GONE
            } else {
                findViewById<MaterialTextView>(R.id.secondary_text).visibility = View.VISIBLE
                findViewById<MaterialTextView>(R.id.secondary_text).text = HtmlUtils.textToHtml(item.secondaryText)
            }
        }
    }
}
