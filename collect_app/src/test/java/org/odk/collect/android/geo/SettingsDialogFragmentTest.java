package org.odk.collect.android.geo;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class SettingsDialogFragmentTest {

    private ActivityController<FragmentActivity> activity;
    private FragmentManager fragmentManager;
    private SettingsDialogFragment dialogFragment;

    @Before
    public void setup() {
        activity = RobolectricHelpers.buildThemedActivity(FragmentActivity.class);
        activity.setup();

        fragmentManager = activity.get().getSupportFragmentManager();
        dialogFragment = new SettingsDialogFragment();
    }

    @Test
    public void dialogIsCancellable() {

    }

    @Test
    public void shouldShowCorrectButtons() {

    }

    @Test
    public void clickingStart_shouldDismissTheDialog() {

    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {

    }
}
