package org.odk.collect.qrcode

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FlashlightToggle(
    flashlightOn: Boolean = false,
    modifier: Modifier = Modifier,
    onFlashlightToggled: () -> Unit = {}
) {
    Button(onClick = { onFlashlightToggled() }, modifier = modifier) {
        if (flashlightOn) {
            Text(stringResource(org.odk.collect.strings.R.string.turn_off_flashlight))
        } else {
            Text(stringResource(org.odk.collect.strings.R.string.turn_on_flashlight))
        }
    }
}

@Preview
@Composable
private fun PreviewFlashlightToggleOn() {
    MaterialTheme {
        FlashlightToggle(true)
    }
}

@Preview
@Composable
private fun PreviewFlashlightToggleOff() {
    MaterialTheme {
        FlashlightToggle(false)
    }
}
