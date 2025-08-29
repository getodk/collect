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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.zxing.client.android.BeepManager
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils.getComponent
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.async.Scheduler
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.FlashlightToggleView
import javax.inject.Inject

abstract class BarCodeScannerFragment : Fragment() {

    @Inject
    lateinit var barcodeScannerViewFactory: BarcodeScannerViewContainer.Factory

    @Inject
    lateinit var scheduler: Scheduler

    private lateinit var barcodeScannerViewContainer: BarcodeScannerViewContainer

    private lateinit var beepManager: BeepManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent(context).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        beepManager = BeepManager(activity)

        val rootView = inflater.inflate(R.layout.fragment_scan, container, false)
        barcodeScannerViewContainer = rootView.findViewById(R.id.barcode_view)
        barcodeScannerViewContainer.setup(
            barcodeScannerViewFactory,
            requireActivity(),
            getViewLifecycleOwner(),
            this.isQrOnly(),
            frontCameraUsed()
        )

        rootView.findViewById<TextView>(R.id.prompt).setText(org.odk.collect.strings.R.string.barcode_scanner_prompt)

        val flashlightToggleView =
            rootView.findViewById<FlashlightToggleView>(R.id.switch_flashlight)
        flashlightToggleView.setup(barcodeScannerViewContainer.barcodeScannerView)
        // if the device does not have flashlight in its camera, then remove the switch flashlight button...
        if (!hasFlash() || frontCameraUsed()) {
            flashlightToggleView.visibility = View.GONE
        }

        barcodeScannerViewContainer.barcodeScannerView.latestBarcode
            .observe(getViewLifecycleOwner()) { result: String ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (_: Exception) {
                    // ignored
                }

                if (shouldConfirm()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        handleScanningResult(result)
                    }, 2000L)
                } else {
                    handleScanningResult(result)
                }
            }

        barcodeScannerViewContainer.barcodeScannerView.start()
        return rootView
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
        scheduler.immediate(true, 2000L) {
            barcodeScannerViewContainer.barcodeScannerView.start()
        }
    }

    protected abstract fun isQrOnly(): Boolean

    protected abstract fun shouldConfirm(): Boolean

    protected abstract fun handleScanningResult(result: String)
}
