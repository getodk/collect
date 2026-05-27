package org.odk.collect.qrcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun ScannerOverlay(
    detectedState: DetectedState = DetectedState.None,
    fullScreenViewFinder: Boolean = false
) {
    BoxWithConstraints {
        val (viewFinderOffset, viewFinderSize) = with(LocalDensity.current) {
            calculateViewFinder(maxWidth.toPx(), maxHeight.toPx(), fullScreenViewFinder)
        }

        val highlightWidth = if (fullScreenViewFinder) {
            16.dp
        } else {
            4.dp
        }

        if (!fullScreenViewFinder) {
            ViewFinder(viewFinderSize, viewFinderOffset)
        }

        ViewFinderHighlight(detectedState, viewFinderSize, viewFinderOffset, highlightWidth)

        if (!fullScreenViewFinder) {
            val topRight = Offset(viewFinderOffset.x + viewFinderSize.width, viewFinderOffset.y).dp()
            val circleRadius = 18.dp
            val circleOffset = DpOffset(x = topRight.x - circleRadius, y = topRight.y - circleRadius)
            ViewFinderIcon(
                detectedState,
                modifier = Modifier
                    .offset(circleOffset.x, circleOffset.y)
                    .size(circleRadius * 2)
            )
        }
    }
}

@Composable
private fun ViewFinderHighlight(
    detectedState: DetectedState,
    viewFinderSize: Size,
    viewFinderOffset: Offset,
    width: Dp
) {
    val density = LocalDensity.current
    val smallShapeCornerSize = MaterialTheme.shapes.small.topStart

    if (detectedState != DetectedState.None) {
        val outlineColor = if (detectedState == DetectedState.Potential) {
            Color(0xFFFFC107)
        } else {
            Color(0xFF9CCC65)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerSizePx = smallShapeCornerSize.toPx(viewFinderSize, density)
            val cornerRadius = CornerRadius(cornerSizePx, cornerSizePx)
            drawRoundRect(
                color = outlineColor,
                topLeft = viewFinderOffset,
                size = viewFinderSize,
                style = Stroke(width = width.toPx()),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
private fun ViewFinderIcon(
    detectedState: DetectedState,
    modifier: Modifier = Modifier
) {
    if (detectedState == DetectedState.None) {
        return
    }

    val lightColor = if (detectedState == DetectedState.Potential) {
        Color(0xFFFAEDC4)
    } else {
        Color(0xFFCAF1D8)
    }

    val tickIcon = rememberVectorPainter(Icons.Default.Check)
    val crossIcon = rememberVectorPainter(Icons.Default.Close)
    val icon = if (detectedState == DetectedState.Potential) {
        crossIcon
    } else {
        tickIcon
    }

    val iconColor = if (detectedState == DetectedState.Potential) {
        Color(0xFFFFA726)
    } else {
        Color(0xFF66BB6A)
    }

    Canvas(modifier = modifier.graphicsLayer(shadowElevation = 10f, shape = CircleShape)) {
        val circleRadius = size.width / 2

        drawCircle(
            lightColor,
            center = Offset(circleRadius, circleRadius),
            radius = circleRadius
        )

        val iconSize = Size(24.dp.toPx(), 24.dp.toPx())
        translate(
            left = (size.width - iconSize.width) / 2,
            top = (size.height - iconSize.height) / 2
        ) {
            with(icon) {
                draw(iconSize, colorFilter = ColorFilter.tint(iconColor))
            }
        }
    }
}

@Composable
private fun ViewFinder(viewFinderSize: Size, offset: Offset) {
    val density = LocalDensity.current
    val smallShapeCornerSize = MaterialTheme.shapes.small.topStart

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Make sure the resulting layer is drawn on screen in one visible move
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        drawRect(
            color = Color(0x80000000),
            size = size
        )

        val cornerSizePx = smallShapeCornerSize.toPx(viewFinderSize, density)
        val cornerRadius = CornerRadius(cornerSizePx, cornerSizePx)
        drawRoundRect(
            color = Color(0x00000000),
            topLeft = offset,
            size = viewFinderSize,
            blendMode = BlendMode.Clear,
            cornerRadius = cornerRadius
        )
    }
}

@Composable
fun Offset.dp(): DpOffset {
    val offset = this
    return with(LocalDensity.current) {
        DpOffset(offset.x.toDp(), offset.y.toDp())
    }
}

fun calculateViewFinder(width: Float, height: Float, fullScreen: Boolean): Pair<Offset, Size> {
    return if (fullScreen) {
        val viewFinderSize = Size(width, height)
        val viewFinderOffset = Offset(0f, 0f)
        Pair(viewFinderOffset, viewFinderSize)
    } else {
        val viewFinderWidth = min(MAX_VIEWFINDER_WIDTH, width / 100 * 75)
        val viewFinderSize = Size(viewFinderWidth, viewFinderWidth)
        val viewFinderOffset = Offset(
            (width - viewFinderSize.width) / 2,
            (height - viewFinderSize.height) / 2
        )

        Pair(viewFinderOffset, viewFinderSize)
    }
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

@Preview
@Composable
private fun PreviewFullScreen() {
    MaterialTheme {
        ScannerOverlay(detectedState = DetectedState.Potential, fullScreenViewFinder = true)
    }
}
