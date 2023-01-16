package org.odk.collect.android.support.actions;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.pm.ActivityInfo;
import android.os.RemoteException;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.Matcher;

public class RotateAction implements ViewAction {

    private final int screenOrientation;

    public RotateAction(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void perform(UiController uiController, View view) {
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        try {
            if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                device.setOrientationLeft();
            } else {
                device.setOrientationNatural();
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
