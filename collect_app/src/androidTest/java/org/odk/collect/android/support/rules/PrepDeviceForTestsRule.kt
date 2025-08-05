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
                setAnimationScale(0)
                setLongPressTimeout(3000)

                try {
                    base.evaluate()
                } finally {
                    setAnimationScale(1)
                    setLongPressTimeout(500)
                }
            }
        }
    }

    private fun setLongPressTimeout(timeout: Int) {
        executeShellCommand("settings put secure long_press_timeout $timeout")
    }

    private fun setAnimationScale(scale: Int) {
        ANIMATIONS.forEach { executeShellCommand("settings put global $it $scale") }
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
