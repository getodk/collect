package org.odk.collect.androidshared.ui

import android.content.DialogInterface
import android.view.KeyEvent

class OnBackPressedKeyListener(private val onBackPressed: () -> Unit) : DialogInterface.OnKeyListener {
    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            if (it.action == KeyEvent.ACTION_DOWN && it.keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                return true
            }
        }

        return false
    }
}
