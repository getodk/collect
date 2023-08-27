package org.odk.collect.android.widgets.utilities

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.utilities.ContentUriProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormEntryPromptUtils
import timber.log.Timber
import java.io.File

object ImageCaptureIntentCreator {
    fun imageCaptureIntent(prompt: FormEntryPrompt, context: Context, tmpImageFilePath: String): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageName = FormEntryPromptUtils.getBindAttribute(prompt, "intent")
        if (packageName != null) {
            intent.setPackage(packageName)
        }

        // The Android Camera application saves a full-size photo if you give it a file to save into.
        // You must provide a fully qualified file name where the camera app should save the photo.
        // https://developer.android.com/training/camera-deprecated/photobasics
        try {
            val uri = ContentUriProvider().getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                File(tmpImageFilePath)
            )
            // if this gets modified, the onActivityResult in
            // FormEntyActivity will also need to be updated.
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            FileUtils.grantFilePermissions(intent, uri, context)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }

        return intent
    }
}
