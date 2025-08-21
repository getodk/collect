package org.odk.collect.qrcode

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color

@Composable
fun ScannerOverlay(viewFinderRect: Rect) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color(0x4B000000),
            size = size
        )

        drawRect(
            color = Color(0x00000000),
            topLeft = Offset(viewFinderRect.left.toFloat(), viewFinderRect.top.toFloat()),
            size = Size(
                (viewFinderRect.right - viewFinderRect.left).toFloat(),
                (viewFinderRect.bottom - viewFinderRect.top).toFloat()
            ),
            blendMode = BlendMode.Clear
        )
    }
}
