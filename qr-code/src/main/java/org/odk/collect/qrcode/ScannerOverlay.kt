package org.odk.collect.qrcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun ScannerOverlay(
    detectedState: DetectedState = DetectedState.None,
    prompt: String = ""
) {
    ViewFinder()
    ViewFinderHighlight(detectedState)

    if (prompt.isNotEmpty() && detectedState !is DetectedState.Full) {
        ScannerPrompt(prompt)
    }
}

@Composable
private fun ViewFinderHighlight(detectedState: DetectedState) {
    val density = LocalDensity.current
    val smallShapeCornerSize = MaterialTheme.shapes.small.topStart
    val tickIcon = rememberVectorPainter(Icons.Default.Check)
    val crossIcon = rememberVectorPainter(Icons.Default.Close)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val (viewFinderOffset, viewFinderSize) = calculateViewFinder(size.width, size.height)

        if (detectedState != DetectedState.None) {
            val darkColor = if (detectedState == DetectedState.Potential) {
                Color(0xFFFFC107)
            } else {
                Color(0xFF9CCC65)
            }
            val lightColor = if (detectedState == DetectedState.Potential) {
                Color(0xFFFAEDC4)
            } else {
                Color(0xFFCAF1D8)
            }

            val cornerSizePx = smallShapeCornerSize.toPx(viewFinderSize, density)
            val cornerRadius = CornerRadius(cornerSizePx, cornerSizePx)
            drawRoundRect(
                color = darkColor,
                topLeft = viewFinderOffset,
                size = viewFinderSize,
                style = Stroke(width = 4.dp.toPx()),
                cornerRadius = cornerRadius
            )

            val circleCenter = Offset(viewFinderOffset.x + viewFinderSize.width, viewFinderOffset.y)
            val circleRadius = 18.dp.toPx()
            drawCircle(
                lightColor,
                center = circleCenter,
                radius = circleRadius
            )

            val icon = if (detectedState == DetectedState.Potential) {
                crossIcon
            } else {
                tickIcon
            }

            val iconSize = Size(24.dp.toPx(), 24.dp.toPx())
            translate(
                left = circleCenter.x - (iconSize.width / 2),
                top = circleCenter.y - (iconSize.height / 2)
            ) {
                icon.apply<Painter> {
                    this@Canvas.draw(iconSize, colorFilter = ColorFilter.tint(darkColor))
                }
            }
        }
    }
}

@Composable
private fun ViewFinder() {
    val density = LocalDensity.current
    val smallShapeCornerSize = MaterialTheme.shapes.small.topStart

    Canvas(modifier = Modifier.fillMaxSize()) {
        val (viewFinderOffset, viewFinderSize) = calculateViewFinder(size.width, size.height)

        drawRect(
            color = Color(0x4B000000),
            size = size
        )

        val cornerSizePx = smallShapeCornerSize.toPx(viewFinderSize, density)
        val cornerRadius = CornerRadius(cornerSizePx, cornerSizePx)
        drawRoundRect(
            color = Color(0x00000000),
            topLeft = viewFinderOffset,
            size = viewFinderSize,
            blendMode = BlendMode.Clear,
            cornerRadius = cornerRadius
        )
    }
}

@Composable
private fun ScannerPrompt(prompt: String) {
    val textMeasurer = rememberTextMeasurer()
    val bodyMediumTextStyle = MaterialTheme.typography.bodyMedium
    val standardMargin = dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val (viewFinderOffset, viewFinderSize) = calculateViewFinder(size.width, size.height)

        val textLayoutResult = textMeasurer.measure(
            prompt,
            bodyMediumTextStyle
        )

        val horizontalMiddleOfViewFinder = viewFinderOffset.x + (viewFinderSize.width / 2)
        val textTopLeft = Offset(
            horizontalMiddleOfViewFinder - (textLayoutResult.size.width / 2),
            viewFinderOffset.y + viewFinderSize.height + standardMargin
                .toPx()
        )

        drawText(
            textLayoutResult,
            color = Color.White,
            topLeft = textTopLeft
        )
    }
}

fun calculateViewFinder(width: Float, height: Float): Pair<Offset, Size> {
    val viewFinderWidth = min(MAX_VIEWFINDER_WIDTH, width / 100 * 75)
    val viewFinderSize = Size(viewFinderWidth, viewFinderWidth)
    val viewFinderOffset = Offset(
        (width - viewFinderSize.width) / 2,
        (height - viewFinderSize.height) / 2
    )

    return Pair(viewFinderOffset, viewFinderSize)
}

private const val MAX_VIEWFINDER_WIDTH = 820f

@Preview
@Composable
private fun PreviewNone() {
    MaterialTheme {
        ScannerOverlay()
    }
}

@Preview
@Composable
private fun PreviewPrompt() {
    MaterialTheme {
        ScannerOverlay(prompt = "I am prompt!")
    }
}

@Preview
@Composable
private fun PreviewPotential() {
    MaterialTheme {
        ScannerOverlay(
            detectedState = DetectedState.Potential
        )
    }
}

@Preview
@Composable
private fun PreviewFull() {
    MaterialTheme {
        ScannerOverlay(
            detectedState = DetectedState.Full(
                DetectedBarcode.Utf8(
                    "",
                    BarcodeFormat.OTHER,
                    byteArrayOf()
                )
            )
        )
    }
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun PreviewSmall() {
    MaterialTheme {
        ScannerOverlay(detectedState = DetectedState.Potential)
    }
}
