package org.odk.collect.android.formhierarchy

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.WidgetAnswer
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.compose.marginSmall
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
                WidgetAnswer(
                    modifier = Modifier.padding(top = marginStandard()),
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
