/* Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.google.zxing.client.android.BeepManager
import org.odk.collect.android.databinding.FragmentScanBinding
import org.odk.collect.android.injection.DaggerUtils.getComponent
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.async.Scheduler
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.FlashlightToggle
import org.odk.collect.qrcode.calculateViewFinder
import org.odk.collect.strings.R
import javax.inject.Inject

abstract class BarCodeScannerFragment : Fragment() {

    @Inject
    lateinit var barcodeScannerViewFactory: BarcodeScannerViewContainer.Factory

    @Inject
    lateinit var scheduler: Scheduler

    private val beepManager: BeepManager by lazy { BeepManager(requireActivity()) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent(context).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentScanBinding.inflate(inflater, container, false)

        binding.barcodeView.setup(
            barcodeScannerViewFactory,
            requireActivity(),
            getViewLifecycleOwner(),
            isQrOnly(),
            frontCameraUsed()
        )

        val flashlightOnState = mutableStateOf(false)
        binding.barcodeView.barcodeScannerView.setTorchListener(object :
            BarcodeScannerView.TorchListener {
            override fun onTorchOn() {
                flashlightOnState.value = true
            }

            override fun onTorchOff() {
                flashlightOnState.value = false
            }
        })

        binding.composeView.setContextThemedContent {
            ScannerControls(
                showFlashLight = hasFlash() && !frontCameraUsed(),
                flashlightOn = flashlightOnState.value,
                onFlashlightToggled = {
                    binding.barcodeView.barcodeScannerView.setTorchOn(!flashlightOnState.value)
                }
            )
        }

        binding.barcodeView.barcodeScannerView.latestBarcode
            .observe(getViewLifecycleOwner()) { result: String ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (_: Exception) {
                    // ignored
                }

                handleScanningResult(result)
            }

        binding.barcodeView.barcodeScannerView.start()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateConfiguration(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateConfiguration(newConfig)
    }

    private fun updateConfiguration(config: Configuration) {
        val binding = FragmentScanBinding.bind(requireView())

        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        binding.barcodeView.barcodeScannerView.setFullScreenViewFinder(isLandscape)
    }

    private fun hasFlash(): Boolean {
        return requireActivity().applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun frontCameraUsed(): Boolean {
        val bundle = requireActivity().intent.extras
        return bundle != null && bundle.getBoolean(Appearances.FRONT)
    }

    protected fun restartScanning() {
        val binding = FragmentScanBinding.bind(requireView())
        scheduler.immediate(true, 2000L) {
            binding.barcodeView.barcodeScannerView.start()
        }
    }

    protected abstract fun isQrOnly(): Boolean

    protected abstract fun handleScanningResult(result: String)
}

@Composable
private fun ScannerControls(
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
