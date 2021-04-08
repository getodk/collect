/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.utilities

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import org.odk.collect.android.application.Collect

object ContentUriHelper {

    @JvmStatic
    fun getIdFromUri(contentUri: Uri): Long {
        val lastSegmentIndex = contentUri.pathSegments.size - 1
        val idSegment = contentUri.pathSegments[lastSegmentIndex]
        return idSegment.toLong()
    }

    @JvmStatic
    fun getFileExtensionFromUri(fileUri: Uri): String? {
        val mimeType = Collect.getInstance().contentResolver.getType(fileUri)
        var extension = if (fileUri.scheme != null && fileUri.scheme == ContentResolver.SCHEME_CONTENT) MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) else MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
        if (extension == null || extension.isEmpty()) {
            Collect.getInstance().contentResolver.query(fileUri, null, null, null, null).use { cursor ->
                var name: String? = null
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                }
                extension = name?.substring(name.lastIndexOf('.') + 1) ?: ""
            }
        }

        if (extension!!.isEmpty() && mimeType != null && mimeType.contains("/")) {
            extension = mimeType.substring(mimeType.lastIndexOf('/') + 1)
        }

        return extension
    }
}
