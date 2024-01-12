package org.odk.collect.android.support.rules

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.testshared.DummyActivity

/**
 * Disables animations and sets long press timeout to 3 seconds in an attempt to avoid flakiness.
 */
class PrepDeviceForTestsRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                disableAnimations()
                increaseLongPressTimeout()
                removeRecentAppsTooltips()
                firstRun = false

                base.evaluate()
            }
        }
    }

    /**
     * Makes sure `Page#killAndReopenApp` doesn't run into problems with tooltips by opening
     * Recent Apps and dismissing before any test runs. Only needs to run once per test process.
     */
    private fun removeRecentAppsTooltips() {
        if (firstRun) {
            val device = UiDevice.getInstance(getInstrumentation())

            // Open dummy activity so there is something in Recent Apps
            getInstrumentation().targetContext.apply {
                val intent = Intent(this.applicationContext, DummyActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                device.wait(Until.hasObject(By.textStartsWith(DummyActivity.TEXT)), 1000)
            }

            // Open Recent Apps and dismiss tooltips if they're there
            device.pressRecentApps()
            val foundToolTip = device.wait(
                Until.hasObject(By.textStartsWith("Select text and images to copy")),
                1000
            )
            if (foundToolTip) {
                device.pressBack() // the first time we open the list of recent apps, a tooltip might be displayed and we need to close it
            }

            // Close recent apps
            device.pressBack()

            // Close dummy activity
            device.pressBack()
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
        var firstRun = true

        private val ANIMATIONS: List<String> = listOf(
            "transition_animation_scale",
            "window_animation_scale",
            "animator_duration_scale"
        )
    }
}
