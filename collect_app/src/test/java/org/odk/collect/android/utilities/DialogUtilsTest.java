package org.odk.collect.android.utilities;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.support.CollectHelpers.buildThemedActivity;
import static org.odk.collect.android.support.CollectHelpers.createThemedActivity;

@RunWith(AndroidJUnit4.class)
public class DialogUtilsTest {

    @Test
    public void showIfNotShowing_onlyEverOpensOneDialog() {
        FragmentActivity activity = createThemedActivity(FragmentActivity.class);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        DialogUtils.showIfNotShowing(DialogFragment.class, fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(1));
        Fragment dialog1 = fragmentManager.getFragments().get(0);

        DialogUtils.showIfNotShowing(DialogFragment.class, fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(1));
        assertThat(fragmentManager.getFragments().get(0), equalTo(dialog1));
    }

    @Test
    public void showIfNotShowing_whenActivitySavedState_doesNotShowDialog() {
        ActivityController<FragmentActivity> activityController = buildThemedActivity(FragmentActivity.class).setup();
        activityController.pause().stop().saveInstanceState(new Bundle());

        FragmentManager fragmentManager = activityController.get().getSupportFragmentManager();
        DialogUtils.showIfNotShowing(DialogFragment.class, fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(0));
    }

    @Test
    public void showIfNotShowing_whenActivityDestroyed_doesNotShowDialog() {
        ActivityController<FragmentActivity> activityController = buildThemedActivity(FragmentActivity.class).setup();
        activityController.pause().stop().destroy();

        FragmentManager fragmentManager = activityController.get().getSupportFragmentManager();
        DialogUtils.showIfNotShowing(DialogFragment.class, fragmentManager);
        assertThat(fragmentManager.getFragments().size(), equalTo(0));
    }
}
