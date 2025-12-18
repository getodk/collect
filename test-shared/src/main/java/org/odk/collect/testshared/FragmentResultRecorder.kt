package org.odk.collect.testshared

import android.os.Bundle
import androidx.fragment.app.FragmentResultListener

class FragmentResultRecorder : FragmentResultListener {

    private val results = mutableListOf<Pair<String, Bundle>>()

    fun getAll(): List<Pair<String, Bundle>> {
        return results
    }

    fun clear() {
        results.clear()
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        results.add(Pair(requestKey, result))
    }
}
