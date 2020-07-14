package org.odk.collect.android.support.pages;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.jetbrains.annotations.NotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NotificationDrawer {

    public static NotificationDrawer open() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        return new NotificationDrawer();
    }

    public NotificationDrawer assertNotification(String appName, String title) {
        UiDevice device = waitForNotification(appName);
        UiObject2 titleElement = device.findObject(By.text(title));
        assertThat(titleElement.getText(), is(title));
        return this;
    }

    public <D extends Page<D>> D clickNotification(String appName, String title, String expectedTextOnClick, D destination) {
        UiDevice device = waitForNotification(appName);
        UiObject2 titleElement = device.findObject(By.text(title));
        titleElement.click();

        device.wait(Until.hasObject(By.textStartsWith(expectedTextOnClick)), 2000L);
        return destination.assertOnPage();
    }

    public void clearAll() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject2 clearAll = device.findObject(By.text("CLEAR ALL"));
        clearAll.click();
    }

    @NotNull
    private UiDevice waitForNotification(String appName) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.wait(Until.hasObject(By.textStartsWith(appName)), 2000L);
        return device;
    }
}
