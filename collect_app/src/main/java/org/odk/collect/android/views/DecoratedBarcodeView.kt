package org.odk.collect.android.views

import android.content.Context
import android.util.AttributeSet
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class DecoratedBarcodeView : DecoratedBarcodeView {
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    private fun initialize() {
        if (test) {
            visibility = INVISIBLE
        }
    }

    companion object {
        var test = false
    }
}
