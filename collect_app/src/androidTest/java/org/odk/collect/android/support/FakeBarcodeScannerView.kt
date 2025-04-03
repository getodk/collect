package org.odk.collect.android.support

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.androidshared.utils.CompressionUtils
import org.odk.collect.qrcode.BarcodeScannerView
import org.odk.collect.qrcode.BarcodeScannerViewContainer

class FakeBarcodeScannerView(context: Context) : BarcodeScannerView(context) {

    private var callback: ((String) -> Unit)? = null

    override fun decodeContinuous(callback: (String) -> Unit) {
        this.callback = callback
    }

    override fun setTorchOn(on: Boolean) = Unit
    override fun setTorchListener(torchListener: TorchListener) = Unit

    fun scan(result: String) {
        post {
            callback?.invoke(result)
        }
    }
}

class FakeBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {

    private val views = mutableListOf<FakeBarcodeScannerView>()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
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
        views.forEach { it.scan(compressedResult) }
    }
}
