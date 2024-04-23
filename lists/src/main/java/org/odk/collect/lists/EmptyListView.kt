package org.odk.collect.lists

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.withStyledAttributes
import org.odk.collect.lists.databinding.EmptyListViewBinding

class EmptyListView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = EmptyListViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.EmptyListView) {
            val icon = this.getResourceId(R.styleable.EmptyListView_icon, 0)
            val title = this.getString(R.styleable.EmptyListView_title)
            val subtitle = this.getString(R.styleable.EmptyListView_subtitle)

            binding.icon.setImageResource(icon)
            binding.title.text = title
            binding.subtitle.text = subtitle
        }
    }

    fun setIcon(@DrawableRes icon: Int) {
        binding.icon.setImageResource(icon)
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setSubtitle(subtitle: String) {
        binding.subtitle.text = subtitle
    }
}
