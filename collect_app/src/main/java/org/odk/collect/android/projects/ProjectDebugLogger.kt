package org.odk.collect.android.projects

import org.odk.collect.shared.DebugLogger
import java.io.File

class ProjectDebugLogger(private val category: String, private val file: File) : DebugLogger {
    override fun log(tag: String, message: String) {
        val line = "${System.currentTimeMillis()} $category $tag:\"$message\""
        file.appendText(line)
    }
}
