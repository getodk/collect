package org.odk.collect.android.support.rules

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

class ResetRotationRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                Timber.d("Resetting rotation...")
                val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                device.setOrientationNatural()
                base.evaluate()
            }
        }
    }
}
