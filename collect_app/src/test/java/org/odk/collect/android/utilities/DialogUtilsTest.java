package org.odk.collect.android.utilities;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.support.RobolectricHelpers.buildThemedActivity;
import static org.odk.collect.android.support.RobolectricHelpers.createThemedActivity;

@RunWith(RobolectricTestRunner.class)
public class DialogUtilsTest {

    @Test
    public void showIfNotShowing_onlyEverOpensOneDialog() {
        FragmentActivity activity = createThemedActivity(FragmentActivity.class);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        DialogFragment dialog1 = new DialogFragment();
        DialogUtils.showIfNotShowing(dialog1, fragmentManager);

        DialogFragment dialog2 = new DialogFragment();
        DialogUtils.showIfNotShowing(dialog2, fragmentManager);

        assertThat(fragmentManager.getFragments().size(), equalTo(1));
        assertThat(fragmentManager.getFragments().get(0), equalTo(dialog1));
    }

    @Test
    public void showIfNotShowing_whenActivitySavedState_doesNotShowDialog() {
        ActivityController<FragmentActivity> activityController = buildThemedActivity(FragmentActivity.class).setup();
        activityController.pause().stop().saveInstanceState(new Bundle());

        FragmentManager fragmentManager = activityController.get().getSupportFragmentManager();
        DialogUtils.showIfNotShowing(new DialogFragment(), fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(0));
    }

    @Test
    public void showIfNotShowing_whenActivityDestroyed_doesNotShowDialog() {
        ActivityController<FragmentActivity> activityController = buildThemedActivity(FragmentActivity.class).setup();
        activityController.pause().stop().destroy();

        FragmentManager fragmentManager = activityController.get().getSupportFragmentManager();
        DialogUtils.showIfNotShowing(new DialogFragment(), fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(0));
    }
}