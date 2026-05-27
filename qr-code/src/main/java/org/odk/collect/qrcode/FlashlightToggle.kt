package org.odk.collect.qrcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FlashlightToggle(
    modifier: Modifier = Modifier,
    flashlightOn: Boolean = false,
    onFlashlightToggled: () -> Unit = {}
) {
    IconButton(onClick = { onFlashlightToggled() }, modifier = modifier) {
        if (flashlightOn) {
            Icon(
                Icons.Filled.FlashlightOn,
                stringResource(org.odk.collect.strings.R.string.turn_off_flashlight),
                tint = Color.White
            )
        } else {
            Icon(
                Icons.Filled.FlashlightOff,
                stringResource(org.odk.collect.strings.R.string.turn_on_flashlight),
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun PreviewFlashlightToggleOn() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            FlashlightToggle(flashlightOn = true)
        }
    }
}

@Preview
@Composable
private fun PreviewFlashlightToggleOff() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            FlashlightToggle(flashlightOn = false)
        }
    }
}
