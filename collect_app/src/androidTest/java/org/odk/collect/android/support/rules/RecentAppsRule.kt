package org.odk.collect.android.support.rules

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.rules.ExternalResource
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.DummyActivityLauncher

class RecentAppsRule : ExternalResource() {

    override fun before() {
        assertTrue(
            "${this.javaClass.simpleName} does not support this API level!",
            SUPPORTED_SDKS.contains(Build.VERSION.SDK_INT)
        )

        if (Build.VERSION.SDK_INT == 30) {
            removeRecentAppsTooltips()
        }
    }

    fun leaveAndKillApp() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        if (Build.VERSION.SDK_INT == 30) {
            device.pressRecentApps()
            device.wait(Until.hasObject(By.descContains("Collect")), 1000)
            device.findObject(UiSelector().descriptionContains("Collect"))
                .swipeUp(10).also {
                    CollectHelpers.simulateProcessRestart() // the process is not restarted automatically (probably to keep the test running) so we have simulate it
                }
        } else if (Build.VERSION.SDK_INT == 34) {
            device.pressHome() // Pressing recent apps does not actually "leave" the app on API 31+ (cause onPause etc). You need to go home or switch apps.
            device.pressRecentApps()
            device.wait(Until.hasObject(By.descContains("Screenshot")), 1000)
            while (!device.wait(Until.hasObject(By.text("Clear all")), 0)) {
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight / 2,
                    device.displayWidth,
                    device.displayHeight / 2,
                    5
                )
            }

            device.findObject(UiSelector().text("Clear all")).click()
            CollectHelpers.simulateProcessRestart() // the process is not restarted automatically (probably to keep the test running) so we have simulate it
        }
    }

    /**
     * Makes sure [leaveAndKillApp] doesn't run into problems with tooltips by opening
     * Recent Apps and dismissing before any test runs. Only needs to run once per test process.
     */
    private fun removeRecentAppsTooltips() {
        // Open dummy activity so there is something in Recent Apps
        DummyActivityLauncher.launch { device ->
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
        }
    }

    companion object {
        private val SUPPORTED_SDKS = listOf(30, 34)
    }
}
