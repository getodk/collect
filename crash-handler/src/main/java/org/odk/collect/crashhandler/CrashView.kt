package org.odk.collect.crashhandler

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.crashhandler.databinding.CrashLayoutBinding

open class CrashView(context: Context) : FrameLayout(context) {

    private var onErrorDismissed: Runnable? = null
    private val binding = CrashLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.okButton.setOnClickListener { dismiss() }
    }

    internal fun setCrash(title: String, message: String?, onErrorDismissed: Runnable? = null) {
        binding.title.text = title
        binding.message.text = message
        this.onErrorDismissed = onErrorDismissed
    }

    open fun dismiss() {
        onErrorDismissed?.run()
    }
}
