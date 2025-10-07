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
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.Fragment
import com.google.zxing.client.android.BeepManager
import org.odk.collect.android.databinding.FragmentScanBinding
import org.odk.collect.android.injection.DaggerUtils.getComponent
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.async.Scheduler
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.ScannerControls
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

abstract class BarCodeScannerFragment : Fragment() {

    @Inject
    lateinit var barcodeScannerViewFactory: BarcodeScannerViewContainer.Factory

    @Inject
    lateinit var scheduler: Scheduler

    private val beepManager: BeepManager by lazy { BeepManager(requireActivity()) }
    private val fullScreenState = mutableStateOf(false)
    private val fullScreenToggleExtendedState = mutableStateOf(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent(context).inject(this)

        scheduler.immediate(delay = 10.seconds.inWholeMilliseconds) {
            fullScreenToggleExtendedState.value = true
        }
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

        val supportsFullScreen =
            binding.barcodeView.barcodeScannerView.supportsFullScreenViewFinder()
        binding.composeView.setContextThemedContent {
            ScannerControls(
                showFlashLight = hasFlash() && !frontCameraUsed(),
                flashlightOn = flashlightOnState.value,
                fullScreenViewFinder = supportsFullScreen && fullScreenState.value,
                showFullScreenToggle = supportsFullScreen,
                fullScreenToggleExtended = fullScreenToggleExtendedState.value,
                onFullScreenToggled = {
                    fullScreenState.value = !fullScreenState.value
                    updateFullScreen(binding, fullScreenState.value)
                },
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
        val binding = FragmentScanBinding.bind(view)
        if (!binding.barcodeView.barcodeScannerView.supportsFullScreenViewFinder()) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        updateConfiguration(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateConfiguration(newConfig)
    }

    private fun updateConfiguration(config: Configuration) {
        val binding = FragmentScanBinding.bind(requireView())

        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        updateFullScreen(binding, isLandscape)
    }

    private fun updateFullScreen(binding: FragmentScanBinding, isFullScreen: Boolean) {
        binding.barcodeView.barcodeScannerView.setFullScreenViewFinder(isFullScreen)
        fullScreenState.value = isFullScreen
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
