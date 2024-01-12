package org.odk.collect.android.support.rules

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.support.WaitFor.wait250ms

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
            // Open browser so there is something in Recent Apps
            getInstrumentation().targetContext.apply {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                wait250ms()
            }

            // Open Recent Apps and dismiss tooltips if they're there
            val device = UiDevice.getInstance(getInstrumentation())
            device.pressRecentApps()
            device.findObject(UiSelector().textContains("Select text and images to copy"))?.apply {
                wait250ms()
                device.pressBack() // the first time we open the list of recent apps, a tooltip might be displayed and we need to close it
            }

            // Close recent apps
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
