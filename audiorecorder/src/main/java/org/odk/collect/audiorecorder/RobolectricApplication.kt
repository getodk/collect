package org.odk.collect.audiorecorder

import android.app.Application

/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
internal class RobolectricApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        clearDependencies() // Make sure we don't carry dependencies over from test to test
    }
}
