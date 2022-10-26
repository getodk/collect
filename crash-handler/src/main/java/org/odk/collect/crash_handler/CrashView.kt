package org.odk.collect.crash_handler

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

class CrashView(context: Context) : FrameLayout(context) {

    private var onErrorDismissed: Runnable? = null

    init {
        inflate(context, R.layout.crash_layout, this)
        findViewById<View>(R.id.ok_button).setOnClickListener { dismiss() }
    }

    fun setCrash(title: String, message: String?, onErrorDismissed: Runnable? = null) {
        findViewById<TextView>(R.id.title).text = title
        findViewById<TextView>(R.id.message).text = message
        this.onErrorDismissed = onErrorDismissed
    }

    fun dismiss() {
        onErrorDismissed?.run()
    }
}
