/*
 * Copyright (C) 2009 University of Washington
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

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.androidshared.system.IntentLauncherImpl
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import timber.log.Timber
import java.io.File

/**
 * Consolidate all interactions with media providers here.
 *
 *
 * The functionality of getPath() was provided by paulburke as described here:
 * See
 * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android
 * -kitkat-new-storage-access-framework for details
 *
 * @author mitchellsundt@gmail.com
 * @author paulburke
 */
class MediaUtils {
    fun deleteMediaFile(imageFile: String) {
        FileUtils.deleteAndReport(File(imageFile))
    }

    fun openFile(context: Context, file: File, expectedMimeType: String?) {
        val contentUri = ContentUriProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW

        val mimeType =
            if (expectedMimeType == null || expectedMimeType.isEmpty()) FileUtils.getMimeType(file)
            else expectedMimeType

        intent.setDataAndType(contentUri, mimeType)
        FileUtils.grantFileReadPermissions(intent, contentUri, context)

        IntentLauncherImpl.launch(context, intent) {
            val message = context.getString(
                R.string.activity_not_found,
                context.getString(R.string.open_file)
            )
            showLongToast(context, message)
            Timber.w(message)
        }
    }

    fun pickFile(activity: Activity, mimeType: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = mimeType
        activity.startActivityForResult(intent, requestCode)
    }

    fun isVideoFile(file: File): Boolean {
        return FileUtils.getMimeType(file).startsWith("video")
    }

    fun isImageFile(file: File): Boolean {
        return FileUtils.getMimeType(file).startsWith("image")
    }

    fun isAudioFile(file: File): Boolean {
        return FileUtils.getMimeType(file).startsWith("audio")
    }
}
