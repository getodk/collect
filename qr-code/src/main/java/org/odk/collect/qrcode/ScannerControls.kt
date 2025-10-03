package org.odk.collect.qrcode

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.odk.collect.strings.R

@Composable
fun ScannerControls(
    showFlashLight: Boolean,
    flashlightOn: Boolean,
    onFlashlightToggled: () -> Unit = {}
) {
    BoxWithConstraints {
        val landscape =
            LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE

        val bottomOfViewFinder = with(LocalDensity.current) {
            val (viewFinderOffset, viewFinderSize) = calculateViewFinder(
                maxWidth.toPx(),
                maxHeight.toPx(),
                false
            )

            viewFinderOffset.y.toDp() + viewFinderSize.height.toDp()
        }

        androidx.constraintlayout.compose.ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (prompt, flashLightToggle) = createRefs()

            if (landscape) {
                val standardMargin =
                    dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)
                Text(
                    stringResource(R.string.barcode_scanner_prompt),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.constrainAs(prompt) {
                        top.linkTo(parent.top, margin = bottomOfViewFinder + standardMargin)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                if (showFlashLight) {
                    FlashlightToggle(
                        flashlightOn = flashlightOn,
                        onFlashlightToggled = onFlashlightToggled,
                        modifier = Modifier.constrainAs(flashLightToggle) {
                            top.linkTo(prompt.bottom, margin = standardMargin)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                    )
                }
            }
        }
    }
}
