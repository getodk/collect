package org.odk.collect.qrcode

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import org.odk.collect.strings.R

@Composable
fun ScannerControls(
    showFlashLight: Boolean,
    flashlightOn: Boolean,
    fullScreenViewFinder: Boolean = false,
    showFullScreenToggle: Boolean = true,
    fullScreenToggleExtended: Boolean = false,
    onFullScreenToggled: () -> Unit = {},
    onFlashlightToggled: () -> Unit = {}
) {
    BoxWithConstraints {
        val bottomOfViewFinder = with(LocalDensity.current) {
            val (viewFinderOffset, viewFinderSize) = calculateViewFinder(
                maxWidth.toPx(),
                maxHeight.toPx(),
                false
            )

            viewFinderOffset.y.toDp() + viewFinderSize.height.toDp()
        }

        val standardMargin =
            dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)

        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val isLandscape =
                LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (showFullScreenToggle) {
                val (fullScreenToggle) = createRefs()
                ExtendedFloatingActionButton(
                    onClick = onFullScreenToggled,
                    icon = {
                        Icon(Icons.Filled.ScreenRotation, stringResource(R.string.rotate_device))
                    },
                    text = { Text(stringResource(R.string.rotate_device)) },
                    expanded = fullScreenToggleExtended,
                    modifier = Modifier
                        .safeDrawingPadding()
                        .constrainAs(fullScreenToggle) {
                            end.linkTo(parent.end, margin = standardMargin)

                            if (isLandscape) {
                                top.linkTo(parent.top, margin = standardMargin)
                            } else {
                                bottom.linkTo(parent.bottom, margin = standardMargin)
                            }
                        }
                )
            }

            if (!fullScreenViewFinder) {
                val (prompt, flashLightToggle) = createRefs()

                if (!isLandscape) {
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
                }

                if (showFlashLight) {
                    FlashlightToggle(
                        flashlightOn = flashlightOn,
                        onFlashlightToggled = onFlashlightToggled,
                        modifier = Modifier
                            .safeDrawingPadding()
                            .constrainAs(flashLightToggle) {
                                if (isLandscape) {
                                    start.linkTo(parent.start, margin = standardMargin)
                                } else {
                                    end.linkTo(parent.end, margin = standardMargin)
                                }

                                top.linkTo(parent.top, margin = standardMargin)
                            }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            ScannerControls(
                showFlashLight = true,
                flashlightOn = false,
                fullScreenViewFinder = false
            )
        }
    }
}

@Preview
@Composable
private fun PreviewFullScreen() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            ScannerControls(
                showFlashLight = true,
                flashlightOn = false,
                fullScreenViewFinder = true
            )
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PreviewLandscape() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            ScannerControls(
                showFlashLight = true,
                flashlightOn = false,
                fullScreenViewFinder = false
            )
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PreviewFullScreenLandscape() {
    MaterialTheme {
        Box(Modifier.background(Color.Gray)) {
            ScannerControls(
                showFlashLight = true,
                flashlightOn = false,
                fullScreenViewFinder = true
            )
        }
    }
}
