package org.odk.collect.androidshared.system

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream

fun Uri.copyToFile(context: Context, dest: File) {
    try {
        context.contentResolver.openInputStream(this)?.use { inputStream ->
            FileOutputStream(dest).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        // ignore
    }
}

fun Uri.getFileExtension(context: Context): String? {
    var extension = getFileName(context)?.substringAfterLast(".", "")

    if (extension.isNullOrEmpty()) {
        val mimeType = context.contentResolver.getType(this)

        extension = if (scheme == ContentResolver.SCHEME_CONTENT) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            MimeTypeMap.getFileExtensionFromUrl(toString())
        }

        if (extension.isNullOrEmpty()) {
            extension = mimeType?.substringAfterLast("/", "")
        }
    }

    return if (extension.isNullOrEmpty()) {
        null
    } else {
        ".$extension"
    }
}

fun Uri.getFileName(context: Context): String? {
    var fileName: String? = null

    try {
        when (scheme) {
            ContentResolver.SCHEME_FILE -> fileName = toFile().name
            ContentResolver.SCHEME_CONTENT -> {
                val cursor = context.contentResolver.query(this, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val fileNameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (fileNameColumnIndex != -1) {
                            fileName = it.getString(fileNameColumnIndex)
                        }
                    }
                }
            }
            ContentResolver.SCHEME_ANDROID_RESOURCE -> {
                // for uris like [android.resource://com.example.app/1234567890]
                val resourceId = lastPathSegment?.toIntOrNull()
                if (resourceId != null) {
                    fileName = context.resources.getResourceName(resourceId)
                } else {
                    // for uris like [android.resource://com.example.app/raw/sample]
                    val packageName = authority
                    if (pathSegments.size >= 2) {
                        val resourceType = pathSegments[0]
                        val resourceEntryName = pathSegments[1]
                        val resId = context.resources.getIdentifier(resourceEntryName, resourceType, packageName)
                        if (resId != 0) {
                            fileName = context.resources.getResourceName(resId)
                        }
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = path?.substringAfterLast("/")
        }
    } catch (e: Exception) {
        // ignore
    }

    return fileName
}
