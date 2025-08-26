package org.odk.collect.qrcode

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ScannerOverlay(viewFinderRect: Rect, detectedState: DetectedState = DetectedState.None) {
    val smallShapeCornerSize = MaterialTheme.shapes.small.topStart
    val density = LocalDensity.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color(0x4B000000),
            size = size
        )

        val viewFinderOffset = Offset(viewFinderRect.left.toFloat(), viewFinderRect.top.toFloat())
        val viewFinderSize = Size(
            (viewFinderRect.right - viewFinderRect.left).toFloat(),
            (viewFinderRect.bottom - viewFinderRect.top).toFloat()
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

        if (detectedState != DetectedState.None) {
            val borderColor = if (detectedState == DetectedState.Potential) {
                Color.Yellow
            } else {
                Color.Green
            }

            drawRoundRect(
                color = borderColor,
                topLeft = viewFinderOffset,
                size = viewFinderSize,
                style = Stroke(width = 4.dp.toPx()),
                cornerRadius = cornerRadius
            )
        }
    }
}
