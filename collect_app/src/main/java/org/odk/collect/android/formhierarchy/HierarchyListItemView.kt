package org.odk.collect.android.formhierarchy

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

class HierarchyListItemView(context: Context, viewType: Int) : FrameLayout(context) {
    init {
        when (viewType) {
            HierarchyItemType.QUESTION.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_question_item, this, true)
            HierarchyItemType.VISIBLE_GROUP.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_group_item, this, true)
            HierarchyItemType.REPEATABLE_GROUP.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_item, this, true)
            HierarchyItemType.REPEAT_INSTANCE.id -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_instance_item, this, true)
        }
    }

    fun setElement(
        item: HierarchyItem,
        viewModelProvider: ViewModelProvider,
        onCLick: () -> Unit
    ) {
        findViewById<MaterialTextView>(R.id.primary_text).text = item.primaryText
        if (item.hierarchyItemType == HierarchyItemType.QUESTION) {
            findViewById<ComposeView>(R.id.answer_view).setContextThemedContent {
                WidgetAnswer(
                    Modifier,
                    item.formEntryPrompt!!,
                    item.answer,
                    summaryView = true,
                    viewModelProvider = viewModelProvider,
                    onClick = onCLick
                )
            }
        }
    }
}
