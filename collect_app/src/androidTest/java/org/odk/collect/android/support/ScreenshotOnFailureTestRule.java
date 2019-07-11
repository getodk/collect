package org.odk.collect.android.support;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

public class ScreenshotOnFailureTestRule extends TestWatcher {

    @Override
    protected void failed(Throwable e, Description description) {
        super.failed(e, description);
        takeScreenshot(description);
    }

    private void takeScreenshot(Description description) {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        String filename = description.getTestClass().getSimpleName() + "-" + description.getMethodName();
        Screengrab.screenshot(filename);
    }
}
