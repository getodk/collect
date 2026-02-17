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

class HierarchyListItemView(context: Context, layoutResId: Int) : FrameLayout(context) {
    init {
        LayoutInflater.from(context).inflate(layoutResId, this, true)
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
