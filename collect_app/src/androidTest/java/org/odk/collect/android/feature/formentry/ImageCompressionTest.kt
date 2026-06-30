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
                stubCaptureReturningImage(hasAction(MediaStore.ACTION_IMAGE_CAPTURE), 2000)
            }.clickOnString(string.capture_image, AssertionFramework.COMPOSE)

        assertSavedImageWasScaledTo(1000)
    }

    @Test
    fun annotateWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .swipeToNextQuestion("Annotate")
            .also {
                stubCaptureReturningImage(hasAction(MediaStore.ACTION_IMAGE_CAPTURE), 2000)
            }.clickOnString(string.capture_image)

        assertSavedImageWasScaledTo(1000)
    }

    @Test
    fun drawWidget_scalesCapturedImageDownToMaxPixels() {
        rule
            .setUpProjectAndCopyForm("image-compression.xml")
            .fillNewForm("image-compression.xml", "image-compression")
            .swipeToNextQuestion("Annotate")
            .swipeToNextQuestion("Draw")
            .also {
                stubCaptureReturningImage(hasComponent(DrawActivity::class.java.name), 2000)
            }.clickOnString(string.draw_image)

        assertSavedImageWasScaledTo(1000)
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
                stubCaptureReturningImage(hasComponent(DrawActivity::class.java.name), 2000)
            }.clickOnString(string.sign_button)

        assertSavedImageWasScaledTo(1000)
    }

    private fun stubCaptureReturningImage(captureIntent: Matcher<Intent>, longEdge: Int) {
        val bitmap = Bitmap.createBitmap(longEdge, longEdge / 2, Bitmap.Config.ARGB_8888)
        val tmpImage = File(StoragePathProvider().getTmpImageFilePath())
        FileOutputStream(tmpImage).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }

        Intents.intending(captureIntent).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )
    }

    private fun assertSavedImageWasScaledTo(longEdge: Int) {
        waitFor {
            val instanceDir = File(StorageUtils.getInstancesDirPath()).listFiles()!!.single()
            val image = instanceDir.listFiles()!!.single { it.extension.equals("jpg", ignoreCase = true) }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(image.absolutePath, options)
            assertThat(maxOf(options.outWidth, options.outHeight), equalTo(longEdge))
        }
    }
}
