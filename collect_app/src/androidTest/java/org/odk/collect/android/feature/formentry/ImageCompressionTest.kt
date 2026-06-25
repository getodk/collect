package org.odk.collect.android.feature.formentry

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.draw.DrawActivity
import org.odk.collect.testshared.WaitFor.waitFor
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.AssertionFramework
import java.io.File
import java.io.FileOutputStream

class ImageCompressionTest {
    private val rule = FormEntryActivityTestRule()

    @get:Rule
    var chain: RuleChain = chain()
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun imageWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .also {
                stubCaptureReturningImage(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            }.clickOnString(string.capture_image, AssertionFramework.COMPOSE)

        assertSavedImageWasScaledDownToMaxPixels()
    }

    @Test
    fun annotateWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .swipeToNextQuestion("Annotate")
            .also {
                stubCaptureReturningImage(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            }.clickOnString(string.capture_image)

        assertSavedImageWasScaledDownToMaxPixels()
    }

    @Test
    fun drawWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .swipeToNextQuestion("Annotate")
            .swipeToNextQuestion("Draw")
            .also {
                stubCaptureReturningImage(hasComponent(DrawActivity::class.java.name))
            }.clickOnString(string.draw_image)

        assertSavedImageWasScaledDownToMaxPixels()
    }

    @Test
    fun signatureWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .swipeToNextQuestion("Annotate")
            .swipeToNextQuestion("Draw")
            .swipeToNextQuestion("Signature")
            .also {
                stubCaptureReturningImage(hasComponent(DrawActivity::class.java.name))
            }.clickOnString(string.sign_button)

        assertSavedImageWasScaledDownToMaxPixels()
    }

    private fun stubCaptureReturningImage(captureIntent: Matcher<Intent>) {
        // Long edge (2000) is twice the form's max image size, so it should be scaled down to 1000.
        val bitmap = Bitmap.createBitmap(2000, 1000, Bitmap.Config.ARGB_8888)
        val tmpImage = File(StoragePathProvider().getTmpImageFilePath())
        FileOutputStream(tmpImage).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }

        Intents.intending(captureIntent).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )
    }

    private fun assertSavedImageWasScaledDownToMaxPixels() {
        waitFor {
            val instanceDir = File(StorageUtils.getInstancesDirPath()).listFiles()!!.single()
            val image = instanceDir.listFiles()!!.single { it.extension.equals("jpg", ignoreCase = true) }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(image.absolutePath, options)
            assertThat(maxOf(options.outWidth, options.outHeight), equalTo(1000))
        }
    }
}
