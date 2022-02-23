package org.odk.collect.geo

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
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
