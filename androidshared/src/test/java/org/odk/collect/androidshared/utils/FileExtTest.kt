package org.odk.collect.androidshared.utils

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class FileExtTest {
    @Test
    fun `calculateSampleSize returns 1 when both dimensions are within the limit`() {
        assertThat(imageFile(80, 40).calculateSampleSize(100), equalTo(1))
    }

    @Test
    fun `calculateSampleSize returns 1 when both dimensions are exactly the limit`() {
        assertThat(imageFile(100, 100).calculateSampleSize(100), equalTo(1))
    }

    @Test
    fun `calculateSampleSize returns the smallest power of 2 that brings the image within the limit`() {
        // 500 / 4 = 125 still exceeds the limit, 500 / 8 = 62 does not
        assertThat(imageFile(500, 375).calculateSampleSize(100), equalTo(8))
        assertThat(imageFile(375, 500).calculateSampleSize(100), equalTo(8))
    }

    @Test
    fun `calculateSampleSize does not downsample when a dimension sits exactly on the limit`() {
        // 200 / 2 = 100 is within the limit, so it stops there rather than halving again
        assertThat(imageFile(200, 200).calculateSampleSize(100), equalTo(2))
    }

    private fun imageFile(width: Int, height: Int): File {
        val file = File.createTempFile("image", ".png")
        file.deleteOnExit()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }
}
