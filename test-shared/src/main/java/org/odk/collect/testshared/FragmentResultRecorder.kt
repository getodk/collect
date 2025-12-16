package org.odk.collect.testshared

import android.os.Bundle
import androidx.fragment.app.FragmentResultListener

class FragmentResultRecorder : FragmentResultListener {

    var result: Pair<String, Bundle>? = null

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        this.result = Pair(requestKey, result)
    }
}
