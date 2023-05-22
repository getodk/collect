package org.odk.collect.androidshared.system

import android.os.Bundle

object OnSavedInstanceStateRegistry {

    private var bundle: Bundle? = null

    fun setState(savedInstanceState: Bundle) {
        bundle = savedInstanceState
    }

    @JvmStatic
    fun getState(savedInstanceState: Bundle?): Bundle? {
        return if (bundle != null) {
            bundle.also {
                bundle = null
            }
        } else {
            savedInstanceState
        }
    }
}
