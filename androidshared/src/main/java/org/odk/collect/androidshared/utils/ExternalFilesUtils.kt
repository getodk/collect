package org.odk.collect.androidshared.utils

import android.content.Context
import java.io.File
import java.io.IOException

object ExternalFilesUtils {

    @JvmStatic
    fun testExternalFilesAccess(context: Context) {
        try {
            val externalFilesDir = context.getExternalFilesDir(null)
            val testFile = File(externalFilesDir.toString() + File.separator + ".test")
            testFile.createNewFile()
            testFile.delete()
        } catch (e: IOException) {
            throw IllegalStateException("App can't write to storage!")
        }
    }
}
