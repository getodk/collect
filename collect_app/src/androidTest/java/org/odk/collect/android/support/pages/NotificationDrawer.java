package org.odk.collect.android.support.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.support.WaitFor;

public class NotificationDrawer {

    private boolean isOpen;

    public NotificationDrawer open() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        isOpen = true;
        return new NotificationDrawer();
    }

    public void teardown() {
        if (isOpen) {
            clearAll();
        }
    }

    public NotificationDrawer assertNotification(String appName, String title) {
        UiDevice device = waitForNotification(appName);
        UiObject2 titleElement = device.findObject(By.text(title));
        assertThat(titleElement.getText(), is(title));

        return this;
    }

    public NotificationDrawer assertNotification(String appName, String title, String body) {
        UiDevice device = waitForNotification(appName);

        UiObject2 titleElement = device.findObject(By.text(title));
        assertThat(titleElement.getText(), is(title));

        UiObject2 bodyElement = device.findObject(By.text(body));
        assertThat(bodyElement.getText(), is(body));
        return this;
    }

    public <D extends Page<D>> D clickNotification(String appName, String title, String expectedTextOnClick, D destination) {
        UiDevice device = waitForNotification(appName);

        UiObject2 titleElement = device.findObject(By.text(title));
        assertThat(titleElement.getText(), is(title));

        titleElement.click();

        device.wait(Until.hasObject(By.textStartsWith(expectedTextOnClick)), 2000L);
        isOpen = false;
        return destination.assertOnPage();
    }

    public void pressBack() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressBack();
        isOpen = false;
    }

    public void clearAll() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        UiObject2 clearAll = device.findObject(By.text("Clear all"));
        if (clearAll != null) {
            clearAll.click();
        } else {
            // "Clear all" doesn't exist because there are notifications to clear - just press back
            pressBack();
        }

        isOpen = false;
    }

    @NotNull
    private UiDevice waitForNotification(String appName) {
        return WaitFor.waitFor(() -> {
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            Boolean result = device.wait(Until.hasObject(By.textStartsWith(appName)), 0L);
            assertThat("No notification for app: " + appName, result, equalTo(true));
            return device;
        });
    }
}
