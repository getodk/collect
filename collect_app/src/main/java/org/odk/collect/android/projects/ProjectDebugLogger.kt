package org.odk.collect.android.projects

import org.odk.collect.shared.DebugLogger
import java.io.File

class ProjectDebugLogger(private val project: String, private val file: File) : DebugLogger {
    override fun log(tag: String, message: String) {
        val line = "${System.currentTimeMillis()} project:$project $tag:\"$message\""
        file.appendText(line)
    }
}
