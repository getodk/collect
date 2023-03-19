package org.odk.collect.android.support.rules

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

class ScreenshotOnFailureTestRule : TestWatcher() {

    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        takeScreenshot(description)
    }

    private fun takeScreenshot(description: Description) {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        val filename = description.testClass.simpleName + "-" + description.methodName
        Screengrab.screenshot(filename)
    }
}
