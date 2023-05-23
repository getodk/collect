package org.odk.collect.androidshared.system

import android.os.Bundle

interface SavedInstanceStateProvider {
    fun getState(savedInstanceState: Bundle?): Bundle?
}
