package org.odk.collect.qrcode

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BarcodeScannerViewContainer(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    lateinit var barcodeScannerView: BarcodeScannerView
        private set

    fun setup(
        factory: Factory,
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean = false,
        prompt: String = "",
        useFrontCamera: Boolean = false
    ) {
        barcodeScannerView =
            factory.create(activity, lifecycleOwner, qrOnly, prompt, useFrontCamera)
        addView(barcodeScannerView)
    }

    interface Factory {
        fun create(
            activity: Activity,
            lifecycleOwner: LifecycleOwner,
            qrOnly: Boolean = false,
            prompt: String = "",
            useFrontCamera: Boolean
        ): BarcodeScannerView
    }
}

abstract class BarcodeScannerView(context: Context) : FrameLayout(context) {

    private val _latestBarcode = MutableLiveData<String>()
    val latestBarcode: LiveData<String> = _latestBarcode

    protected abstract fun scan(callback: (String) -> Unit)
    abstract fun setTorchOn(on: Boolean)
    abstract fun setTorchListener(torchListener: TorchListener)

    fun start() {
        this.scan { result ->
            _latestBarcode.value = result
        }
    }

    interface TorchListener {
        fun onTorchOn()
        fun onTorchOff()
    }
}
