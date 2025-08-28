package org.odk.collect.qrcode

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.qrcode.databinding.FlashlightToggleLayoutBinding

class FlashlightToggleView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val flashlightOnState = mutableStateOf(false)
    private var barcodeScannerView: BarcodeScannerView? = null

    init {
        FlashlightToggleLayoutBinding.inflate(LayoutInflater.from(context)).also {
            it.composeView.setContextThemedContent {
                FlashlightToggle(
                    flashlightOn = remember { flashlightOnState }.value,
                    onFlashlightToggled = {
                        barcodeScannerView?.setTorchOn(!flashlightOnState.value)
                    }
                )
            }

            addView(it.root)
        }
    }

    fun setup(barcodeScannerView: BarcodeScannerView) {
        barcodeScannerView.setTorchListener(object : BarcodeScannerView.TorchListener {
            override fun onTorchOn() {
                flashlightOnState.value = true
            }

            override fun onTorchOff() {
                flashlightOnState.value = false
            }
        })

        this.barcodeScannerView = barcodeScannerView
    }
}

@Composable
fun FlashlightToggle(flashlightOn: Boolean = false, onFlashlightToggled: () -> Unit = {}) {
    Button(onClick = { onFlashlightToggled() }) {
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
