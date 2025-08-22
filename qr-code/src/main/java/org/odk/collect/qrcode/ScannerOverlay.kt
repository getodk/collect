package org.odk.collect.qrcode

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun ScannerOverlay(viewFinderRect: Rect, detectedState: DetectedState = DetectedState.None) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color(0x4B000000),
            size = size
        )

        drawRoundRect(
            color = Color(0x00000000),
            topLeft = Offset(viewFinderRect.left.toFloat(), viewFinderRect.top.toFloat()),
            size = Size(
                (viewFinderRect.right - viewFinderRect.left).toFloat(),
                (viewFinderRect.bottom - viewFinderRect.top).toFloat()
            ),
            blendMode = BlendMode.Clear,
            cornerRadius = CornerRadius(5f, 5f)
        )

        if (detectedState != DetectedState.None) {
            val borderColor = if (detectedState == DetectedState.Potential) {
                Color.Yellow
            } else {
                Color.Green
            }

            drawRoundRect(
                color = borderColor,
                topLeft = Offset(viewFinderRect.left.toFloat(), viewFinderRect.top.toFloat()),
                size = Size(
                    (viewFinderRect.right - viewFinderRect.left).toFloat(),
                    (viewFinderRect.bottom - viewFinderRect.top).toFloat()
                ),
                style = Stroke(width = 8f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
    }
}
