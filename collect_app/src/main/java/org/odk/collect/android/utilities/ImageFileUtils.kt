package org.odk.collect.android.utilities

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import kotlin.math.ceil

object ImageFileUtils {
    // 80% JPEG quality gives a greater file size reduction with almost no loss in quality
    private const val IMAGE_COMPRESS_QUALITY = 80

    private val EXIF_ORIENTATION_ROTATIONS = arrayOf(
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_180,
        ExifInterface.ORIENTATION_ROTATE_270
    )

    @JvmStatic
    fun saveBitmapToFile(bitmap: Bitmap?, path: String) {
        val compressFormat =
            if (path.lowercase(Locale.getDefault()).endsWith(".png"))
                CompressFormat.PNG
            else CompressFormat.JPEG
        try {
            if (bitmap != null) {
                FileOutputStream(path).use { out -> bitmap.compress(compressFormat, IMAGE_COMPRESS_QUALITY, out) }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /*
    This method is used to avoid OutOfMemoryError exception during loading an image.
    If the exception occurs we catch it and try to load a smaller image.
     */
    @JvmStatic
    fun getBitmap(path: String?, originalOptions: BitmapFactory.Options): Bitmap? {
        val newOptions = BitmapFactory.Options()
        newOptions.inSampleSize = originalOptions.inSampleSize
        if (newOptions.inSampleSize <= 0) {
            newOptions.inSampleSize = 1
        }
        val bitmap: Bitmap? = try {
            BitmapFactory.decodeFile(path, originalOptions)
        } catch (e: OutOfMemoryError) {
            Timber.i(e)
            newOptions.inSampleSize++
            return getBitmap(path, newOptions)
        }
        return bitmap
    }

    @JvmStatic
    fun getBitmapScaledToDisplay(file: File, screenHeight: Int, screenWidth: Int): Bitmap? {
        return getBitmapScaledToDisplay(file, screenHeight, screenWidth, false)
    }

    /**
     * Scales image according to the given display
     *
     * @param file           containing the image
     * @param screenHeight   height of the display
     * @param screenWidth    width of the display
     * @param upscaleEnabled determines whether the image should be up-scaled or not
     * if the window size is greater than the image size
     * @return scaled bitmap
     */
    @JvmStatic
    fun getBitmapScaledToDisplay(
        file: File,
        screenHeight: Int,
        screenWidth: Int,
        upscaleEnabled: Boolean
    ): Bitmap? {
        // Determine image size of file
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        getBitmap(file.absolutePath, options)
        var bitmap: Bitmap?
        val scale: Double
        if (upscaleEnabled) {
            // Load full size bitmap image
            options = BitmapFactory.Options()
            options.inInputShareable = true
            options.inPurgeable = true
            bitmap = getBitmap(file.absolutePath, options)
            val heightScale = options.outHeight.toDouble() / screenHeight
            val widthScale = options.outWidth.toDouble() / screenWidth
            scale = widthScale.coerceAtLeast(heightScale)
            val newHeight = ceil(options.outHeight / scale)
            val newWidth = ceil(options.outWidth / scale)
            if (bitmap != null) {
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    newWidth.toInt(), newHeight.toInt(), false
                )
            }
        } else {
            val heightScale = options.outHeight / screenHeight
            val widthScale = options.outWidth / screenWidth

            // Powers of 2 work faster, sometimes, according to the doc.
            // We're just doing closest size that still fills the screen.
            scale = widthScale.coerceAtLeast(heightScale).toDouble()

            // get bitmap with scale ( < 1 is the same as 1)
            options = BitmapFactory.Options()
            options.inInputShareable = true
            options.inPurgeable = true
            options.inSampleSize = scale.toInt()
            bitmap = getBitmap(file.absolutePath, options)
        }
        if (bitmap != null) {
            Timber.i(
                "Screen is %dx%d.  Image has been scaled down by %f to %dx%d",
                screenHeight, screenWidth, scale, bitmap.height, bitmap.width
            )
        }
        return bitmap
    }

    /**
     * While copying the file, apply the exif rotation of sourceFile to destinationFile
     * so that sourceFile with EXIF has same orientation as destinationFile without EXIF
     */
    @JvmStatic
    fun copyImageAndApplyExifRotation(sourceFile: File, destFile: File) {
        var sourceFileExif: ExifInterface? = null
        try {
            sourceFileExif = ExifInterface(sourceFile)
        } catch (e: IOException) {
            Timber.w(e)
        }
        if (sourceFileExif == null ||
            !EXIF_ORIENTATION_ROTATIONS.contains(
                    sourceFileExif
                        .getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED
                            )
                )
        ) {
            // Source Image doesn't have any EXIF Rotations, so a normal file copy will suffice
            FileUtils.copyFile(sourceFile, destFile)
        } else {
            val sourceImage = getBitmap(sourceFile.absolutePath, BitmapFactory.Options())
            val orientation = sourceFileExif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmapAndSaveToFile(
                    sourceImage,
                    90,
                    destFile.absolutePath
                )
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmapAndSaveToFile(
                    sourceImage,
                    180,
                    destFile.absolutePath
                )
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmapAndSaveToFile(
                    sourceImage,
                    270,
                    destFile.absolutePath
                )
            }
        }
    }

    private fun rotateBitmapAndSaveToFile(image: Bitmap?, degrees: Int, filePath: String) {
        var imageToSave = image
        try {
            val matrix = Matrix()
            matrix.postRotate(degrees.toFloat())
            if (image != null) {
                imageToSave = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
            }
        } catch (e: OutOfMemoryError) {
            Timber.w(e)
        }
        saveBitmapToFile(imageToSave, filePath)
    }
}
