package org.odk.collect.android.support.rules

import org.junit.rules.ExternalResource
import org.odk.collect.android.support.DummyActivityLauncher

class ResetRotationRule : ExternalResource() {

    override fun before() {
        // Some devices are always portrait at the home screen so we need to launch something
        DummyActivityLauncher.launch { device ->
            device.setOrientationNatural()
            device.pressBack()
        }
    }
}
