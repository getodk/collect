package org.odk.collect.android.support.rules

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DisableDeviceAnimationsRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                ANIMATIONS.forEach { executeShellCommand("settings put global $it 0") }
                base.evaluate()
            }
        }
    }

    private fun executeShellCommand(command: String) {
        UiDevice.getInstance(getInstrumentation()).executeShellCommand(command)
    }
}

private val ANIMATIONS: List<String> = listOf(
    "transition_animation_scale",
    "window_animation_scale",
    "animator_duration_scale"
)
