package org.odk.collect.androidshared.ui.multiclicksafe

import android.view.View

abstract class MultiClickSafeOnClickListener : View.OnClickListener {

    abstract fun onSafeClick(v: View)

    override fun onClick(v: View) {
        if (MultiClickGuard.allowClick()) {
            onSafeClick(v)
        }
    }
}

fun View.setMultiClickSafeOnClickListener(listener: (View) -> Unit) {
    setOnClickListener(object : MultiClickSafeOnClickListener() {
        override fun onSafeClick(v: View) {
            listener(v)
        }
    })
}
