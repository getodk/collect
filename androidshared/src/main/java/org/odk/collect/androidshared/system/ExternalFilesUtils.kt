package org.odk.collect.androidshared.system

import android.content.Context
import java.io.File
import java.io.IOException

object ExternalFilesUtils {

    @JvmStatic
    fun testExternalFilesAccess(context: Context) {
        val externalFilesDir = context.getExternalFilesDir(null)

        if (externalFilesDir == null) {
            throw IllegalStateException("External files dir is null!")
        } else {
            try {
                val testFile = File(externalFilesDir, ".test")
                testFile.createNewFile()
                testFile.delete()
            } catch (e: IOException) {
                if (!externalFilesDir.exists()) {
                    throw IllegalStateException(
                        "External files dir does not exist: ${externalFilesDir.absolutePath}"
                    )
                } else {
                    throw IllegalStateException(
                        "App can't write to external files dir: ${externalFilesDir.absolutePath}",
                        e
                    )
                }
            }
        }
    }
}
