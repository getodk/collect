package org.odk.collect.qrcode

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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
    protected abstract fun decodeContinuous(callback: (String) -> Unit)
    abstract fun setTorchOn(on: Boolean)
    abstract fun setTorchListener(torchListener: TorchListener)

    fun waitForBarcode(scope: CoroutineScope): LiveData<String> {
        val liveData = MutableLiveData<String>()
        var acceptResult = true

        this.decodeContinuous { result ->
            if (acceptResult) {
                acceptResult = false
                liveData.value = result
                scope.launch {
                    delay(1.seconds)
                    acceptResult = true
                }
            }
        }

        return liveData
    }

    interface TorchListener {
        fun onTorchOn()
        fun onTorchOff()
    }
}
