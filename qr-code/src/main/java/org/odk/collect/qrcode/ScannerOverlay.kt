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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun Preview() {
    ScannerOverlay(calculateViewFinderRect(400.dp, 400.dp))
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun PreviewPotential() {
    ScannerOverlay(calculateViewFinderRect(400.dp, 400.dp), detectedState = DetectedState.Potential)
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun PreviewFull() {
    ScannerOverlay(
        calculateViewFinderRect(400.dp, 400.dp),
        detectedState = DetectedState.Full(
            DetectedBarcode.Utf8(
                "",
                BarcodeFormat.OTHER,
                byteArrayOf()
            )
        )
    )
}

@Composable
private fun calculateViewFinderRect(width: Dp, height: Dp): Rect {
    return with(LocalDensity.current) {
        val margin = 50.dp

        Rect(
            margin.toPx().toInt(),
            margin.toPx().toInt(),
            (width - margin).toPx().toInt(),
            (height - margin).toPx().toInt()
        )
    }
}
