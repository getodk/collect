package org.odk.collect.android.configure.qr

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.android.preferences.SettingsExt.getExperimentalOptIn
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer
import org.odk.collect.qrcode.mlkit.PlayServicesFallbackBarcodeScannerViewFactory
import org.odk.collect.qrcode.zxing.ZxingBarcodeScannerViewFactory
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class SettingsBarcodeScannerViewFactory(
    private val settings: Settings
) : BarcodeScannerViewContainer.Factory {
    private val playServicesFallbackFactory = PlayServicesFallbackBarcodeScannerViewFactory(2)
    private val zxingFactory = ZxingBarcodeScannerViewFactory()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        val factory = if (settings.getExperimentalOptIn(ProjectKeys.KEY_ZXING_SCANNING)) {
            zxingFactory
        } else {
            playServicesFallbackFactory
        }

        return factory.create(activity, lifecycleOwner, qrOnly, useFrontCamera)
    }
}
