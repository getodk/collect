package org.odk.collect.android.projects

import org.odk.collect.android.BuildConfig
import org.odk.collect.shared.DebugLogger
import java.io.File

class FileDebugLogger(private val file: File) : DebugLogger {

    override fun log(tag: String, message: String) {
        if (enabled) {
            val line = "${System.currentTimeMillis()} $tag \"$message\""
            file.appendText(line)
        }
    }

    companion object {
        private val enabled = BuildConfig.DEBUG
    }
}
