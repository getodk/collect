package org.odk.collect.geo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import org.odk.collect.geo.databinding.SelectionSummarySheetLayoutBinding
import org.odk.collect.strings.localization.LocalizedActivity

abstract class SelectionMapActivity : LocalizedActivity() {

    companion object {
        const val EXTRA_SELECTED_ID = "selected_id"
        const val EXTRA_NEW_ITEM = "new_item"
    }

    fun createNewItem() {
        startActivity(intent.getParcelableExtra(EXTRA_NEW_ITEM))
    }

    fun returnItem(itemId: Long) {
        val data = Intent().also {
            it.putExtra(EXTRA_SELECTED_ID, itemId)
        }

        setResult(RESULT_OK, data)
        finish()
    }
}

abstract class SelectItemFromMap<T> : ActivityResultContract<T, Long?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Long? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getLongExtra(SelectionMapActivity.EXTRA_SELECTED_ID, -1)
        } else {
            null
        }
    }
}

data class MappableSelectItem(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val smallIcon: Int,
    val largeIcon: Int,
    val name: String,
    val status: IconifiedText,
    val info: String?,
    val action: IconifiedText?
) {
    data class IconifiedText(val icon: Int, val text: String)
}

class SelectionSummarySheet(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val binding =
        SelectionSummarySheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: Listener? = null

    private var itemId: Long? = null

    init {
        binding.action.setOnClickListener(::onActionClick)
    }

    fun setItem(item: MappableSelectItem) {
        itemId = item.id

        binding.name.text = item.name

        binding.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, item.status.icon))
        binding.statusIcon.background = null
        binding.statusText.text = item.status.text

        if (item.info != null) {
            binding.info.text = item.info
            binding.info.visibility = View.VISIBLE
            binding.action.visibility = View.GONE
        } else if (item.action != null) {
            binding.action.text = item.action.text
            binding.action.chipIcon = ContextCompat.getDrawable(context, item.action.icon)
            binding.action.visibility = View.VISIBLE
            binding.info.visibility = View.GONE
        }
    }

    private fun onActionClick(view: View) {
        itemId?.let { listener?.selectionAction(it) }
    }

    interface Listener {
        fun selectionAction(id: Long)
    }
}
