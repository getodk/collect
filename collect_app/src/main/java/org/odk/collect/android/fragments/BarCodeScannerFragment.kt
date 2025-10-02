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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.zxing.client.android.BeepManager
import org.odk.collect.android.databinding.FragmentScanBinding
import org.odk.collect.android.injection.DaggerUtils.getComponent
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.calculateViewFinder
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

        binding.switchFlashlight.setup(binding.barcodeView.barcodeScannerView)
        // if the device does not have flashlight in its camera, then remove the switch flashlight button...
        if (!hasFlash() || frontCameraUsed()) {
            binding.switchFlashlight.visibility = View.GONE
        }

        binding.barcodeView.barcodeScannerView.latestBarcode
            .observe(getViewLifecycleOwner()) { result: String ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (_: Exception) {
                    // ignored
                }

                binding.prompt.visibility = View.GONE
                binding.switchFlashlight.visibility = View.GONE

                if (shouldConfirm()) {
                    SnackbarUtils.showSnackbar(
                        binding.root,
                        getString(org.odk.collect.strings.R.string.barcode_scanned),
                        duration = 2000,
                        action = SnackbarUtils.Action(
                            getString(org.odk.collect.strings.R.string.exit_scanning)
                        ) {
                            handleScanningResult(result)
                        },
                        onDismiss = {
                            handleScanningResult(result)
                        }
                    )
                } else {
                    handleScanningResult(result)
                }
            }

        binding.barcodeView.barcodeScannerView.start()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentScanBinding.bind(view)

        // Layout the prompt/flashlight button under the view finder
        view.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            val (offset, size) = calculateViewFinder(
                view.width.toFloat(),
                view.height.toFloat(),
                false // We hide these views in landscape/full screen mode
            )
            val bottomOfViewFinder = offset.y + size.height

            val promptView = binding.prompt
            val promptLayoutParams = promptView.layoutParams as ConstraintLayout.LayoutParams
            val standardMargin =
                resources.getDimension(org.odk.collect.androidshared.R.dimen.margin_standard)
            promptView.layoutParams = ConstraintLayout.LayoutParams(promptLayoutParams).also {
                it.topMargin = (bottomOfViewFinder + standardMargin).toInt()
            }
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
        binding.barcodeView.barcodeScannerView.setFullScreenViewFinder(isLandscape)

        if (isLandscape) {
            binding.prompt.visibility = View.GONE
            binding.switchFlashlight.visibility = View.GONE
        } else {
            binding.prompt.visibility = View.VISIBLE
            binding.switchFlashlight.visibility = View.VISIBLE
        }
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

    protected abstract fun shouldConfirm(): Boolean

    protected abstract fun handleScanningResult(result: String)
}
