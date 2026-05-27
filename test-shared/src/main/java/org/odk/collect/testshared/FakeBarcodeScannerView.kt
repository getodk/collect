package org.odk.collect.testshared

import android.app.Activity
import android.content.Context
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.androidshared.utils.CompressionUtils
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer

class FakeBarcodeScannerView(context: Context) : BarcodeScannerView(context) {

    var isScanning = false
        private set

    private var callback: ((String) -> Unit)? = null

    override fun scan(callback: (String) -> Unit) {
        isScanning = true
        this.callback = callback
    }

    override fun setTorchOn(on: Boolean) = Unit
    override fun setTorchListener(torchListener: TorchListener) = Unit
    override fun supportsFullScreenViewFinder(): Boolean {
        return false
    }

    override fun setFullScreenViewFinder(fullScannerViewFinder: Boolean) = Unit

    fun scan(result: String) {
        isScanning = false

        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback?.invoke(result)
        } else {
            post {
                callback?.invoke(result)
            }
        }
    }
}

class FakeBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {

    private val views = mutableListOf<FakeBarcodeScannerView>()

    val isScanning: Boolean
        get() = views.any { it.isScanning }

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        return FakeBarcodeScannerView(activity).also {
            views.add(it)
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    views.remove(it)
                }
            })
        }
    }

    fun scan(result: String) {
        val compressedResult = CompressionUtils.compress(result)
        views.forEach {
            if (it.isScanning) {
                it.scan(compressedResult)
            }
        }
    }
}
