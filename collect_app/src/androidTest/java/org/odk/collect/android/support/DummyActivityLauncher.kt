package org.odk.collect.android.support

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.odk.collect.testshared.DummyActivity

object DummyActivityLauncher {

    fun launch(block: (UiDevice) -> Unit) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        InstrumentationRegistry.getInstrumentation().targetContext.apply {
            val intent = Intent(this.applicationContext, DummyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            device.wait(Until.hasObject(By.textStartsWith(DummyActivity.TEXT)), 1000)
        }

        block(device)
        device.pressBack()
    }
}
