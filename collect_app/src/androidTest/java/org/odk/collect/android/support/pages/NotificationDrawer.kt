package org.odk.collect.android.support.pages

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.odk.collect.android.support.WaitFor.waitFor

class NotificationDrawer {
    private var isOpen = false

    fun open(): NotificationDrawer {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()
        isOpen = true
        return NotificationDrawer()
    }

    fun teardown() {
        if (isOpen) {
            clearAll()
        }
    }

    @JvmOverloads
    fun assertNotification(
        appName: String,
        title: String,
        body: String? = null,
        subtext: String? = null
    ): NotificationDrawer {
        val device = waitForNotification(appName)

        val titleElement = device.findObject(By.text(title))
        assertThat(titleElement.text, `is`(title))

        body?.let {
            val bodyElement = device.findObject(By.text(body))
            assertThat(bodyElement.text, `is`(body))
        }

        subtext?.let {
            val subtextElement = device.findObject(By.text(subtext))
            assertThat(subtextElement.text, `is`(subtext))
        }
        return this
    }

    fun <D : Page<D>?> clickNotification(
        appName: String,
        title: String,
        expectedTextOnClick: String?,
        destination: D
    ): D {
        val device = waitForNotification(appName)
        val titleElement = device.findObject(By.text(title))
        assertThat(titleElement.text, `is`(title))
        titleElement.click()
        device.wait(Until.hasObject(By.textStartsWith(expectedTextOnClick)), 2000L)
        isOpen = false
        return destination!!.assertOnPage()
    }

    fun pressBack() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()
        isOpen = false
    }

    fun clearAll() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val clearAll = device.findObject(By.text("Clear all"))
        if (clearAll != null) {
            clearAll.click()
        } else {
            // "Clear all" doesn't exist because there are notifications to clear - just press back
            pressBack()
        }
        isOpen = false
    }

    private fun waitForNotification(appName: String): UiDevice {
        return waitFor {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val result = device.wait(Until.hasObject(By.textStartsWith(appName)), 0L)
            assertThat(
                "No notification for app: $appName",
                result,
                `is`(true)
            )
            device
        }
    }
}
