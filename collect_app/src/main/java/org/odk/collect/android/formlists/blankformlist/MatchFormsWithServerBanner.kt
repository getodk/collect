package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.odk.collect.android.databinding.MatchFormsWithServerBannerBinding
import org.odk.collect.strings.R
import java.text.SimpleDateFormat
import java.util.Locale

class MatchFormsWithServerBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding = MatchFormsWithServerBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun update(
        lastMatchFormsWithServerCompletionTime: Long?,
        lastMatchFormsWithServerCompleted: Boolean,
        isSyncing: Boolean
    ) {
        with(binding) {
            if (isSyncing) {
                title.text = context.getString(R.string.sync_in_progress_title)
                subtext.text = context.getString(R.string.sync_in_progress_message)
                subtext.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                refresh.visibility = View.GONE
            } else {
                if (lastMatchFormsWithServerCompleted) {
                    title.text = context.getString(R.string.last_sync_completed)
                } else {
                    title.text = context.getString(R.string.last_sync_failed)
                }
                if (lastMatchFormsWithServerCompletionTime != null) {
                    subtext.text = SimpleDateFormat(
                        context.getString(R.string.last_sync_completion_time),
                        Locale.getDefault()
                    ).format(lastMatchFormsWithServerCompletionTime)
                    subtext.visibility = View.VISIBLE
                } else {
                    subtext.visibility = View.GONE
                }
                progressBar.visibility = View.GONE
                refresh.visibility = View.VISIBLE
            }
        }
    }

    fun setListener(onRefresh: () -> Unit) {
        binding.refresh.setOnClickListener {
            onRefresh()
        }
    }
}
