package org.odk.collect.android.feature.formentry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.android.utilities.FileUtils.getResourceAsStream

class ImageLoadingTest {
    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun updatedImagesAreLoadedCorrectly() {
        rule.startAtMainMenu()
            .copyForm("form_with_images.xml", listOf("rects_v1/rect.jpg", "rects_v1/rect.svg"))
            .startBlankForm("form_with_images")
            .assertImageViewShowsImage(
                R.id.imageView,
                BitmapFactory.decodeStream(getResourceAsStream("media/rects_v1/rect.jpg"))
            )
            .swipeToNextQuestion("SVG")
            .assertImageViewShowsImage(
                R.id.imageView,
                getBitmapFromSvg("media/rects_v1/rect.svg")
            )
            .pressBackAndDiscardForm()
            .copyForm("form_with_images.xml", listOf("rects_v2/rect.jpg", "rects_v2/rect.svg"))
            .startBlankForm("form_with_images")
            .assertImageViewShowsImage(
                R.id.imageView,
                BitmapFactory.decodeStream(getResourceAsStream("media/rects_v2/rect.jpg"))
            )
            .swipeToNextQuestion("SVG")
            .assertImageViewShowsImage(
                R.id.imageView,
                getBitmapFromSvg("media/rects_v2/rect.svg")
            )
    }

    private fun getBitmapFromSvg(filePath: String): Bitmap {
        val svg = SVG.getFromInputStream(getResourceAsStream(filePath))

        val bitmap = Bitmap.createBitmap(
            svg.documentWidth.toInt(),
            svg.documentHeight.toInt(),
            Bitmap.Config.ARGB_8888
        )

        svg.renderToCanvas(Canvas(bitmap))
        return bitmap
    }
}
