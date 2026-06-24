package org.odk.collect.android.formhierarchy

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.compose.marginStandard

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
                val currentView = LocalView.current

                // A ComposeView reused from the RecyclerView pool gets measured to 0 height on
                // rebind, so the row renders blank after scrolling. Nudge a remeasure after
                // composition to apply the real height.
                SideEffect {
                    currentView.requestLayout()
                }

                WidgetAnswer(
                    modifier = Modifier.padding(top = marginStandard()),
                    prompt = item.formEntryPrompt,
                    answer = item.secondaryText,
                    compact = true,
                    mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
                    onClick = onCLick
                )
            }
        }
    }
}
