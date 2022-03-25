package org.odk.collect.geo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.odk.collect.geo.databinding.SelectionSummarySheetLayoutBinding

internal class SelectionSummarySheet(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    val binding =
        SelectionSummarySheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: Listener? = null

    private var itemId: Long? = null

    init {
        binding.action.setOnClickListener {
            itemId?.let { listener?.selectionAction(it) }
        }
    }

    fun setItem(item: MappableSelectItem) {
        itemId = item.id

        binding.name.text = item.name

        binding.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, item.status.icon))
        binding.statusIcon.background = null
        binding.statusText.text = item.status.text

        when (item) {
            is MappableSelectItem.WithAction -> {
                binding.action.text = item.action.text
                binding.action.chipIcon = ContextCompat.getDrawable(context, item.action.icon)
                binding.action.visibility = View.VISIBLE
                binding.info.visibility = View.GONE
            }

            is MappableSelectItem.WithInfo -> {
                binding.info.text = item.info
                binding.info.visibility = View.VISIBLE
                binding.action.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun selectionAction(id: Long)
    }
}
