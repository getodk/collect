package org.odk.collect.android.support.rules

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Disables animations and sets long press timeout to 3 seconds in an attempt to avoid flakiness.
 */
class PrepDeviceForTestsRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                disableAnimations()
                increaseLongPressTimeout()

                base.evaluate()
            }
        }
    }

    private fun increaseLongPressTimeout() {
        executeShellCommand("settings put secure long_press_timeout 3000")
    }

    private fun disableAnimations() {
        ANIMATIONS.forEach { executeShellCommand("settings put global $it 0") }
    }

    private fun executeShellCommand(command: String) {
        UiDevice.getInstance(getInstrumentation()).executeShellCommand(command)
    }

    companion object {
        private val ANIMATIONS: List<String> = listOf(
            "transition_animation_scale",
            "window_animation_scale",
            "animator_duration_scale"
        )
    }
}
