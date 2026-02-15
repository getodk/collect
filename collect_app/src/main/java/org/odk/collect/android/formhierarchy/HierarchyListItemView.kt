package org.odk.collect.android.formhierarchy

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

class HierarchyListItemView(context: Context, viewType: Int) : FrameLayout(context) {
    init {
        when (viewType) {
            HierarchyItem.Question.ID -> LayoutInflater.from(context).inflate(R.layout.hierarchy_question_item, this, true)
            HierarchyItem.VisibleGroup.ID -> LayoutInflater.from(context).inflate(R.layout.hierarchy_group_item, this, true)
            HierarchyItem.RepeatableGroup.ID -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_item, this, true)
            HierarchyItem.RepeatInstance.ID -> LayoutInflater.from(context).inflate(R.layout.hierarchy_repeatable_group_instance_item, this, true)
        }
    }

    fun setElement(
        item: HierarchyItem,
        mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
        onCLick: () -> Unit
    ) {
        findViewById<MaterialTextView>(R.id.primary_text).text = item.primaryText
        if (item is HierarchyItem.Question) {
            findViewById<ComposeView>(R.id.answer_view).setContextThemedContent {
                WidgetAnswer(
                    prompt = item.formEntryPrompt,
                    answer = item.secondaryText,
                    summaryView = true,
                    mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
                    onClick = onCLick
                )
            }
        }
    }
}
