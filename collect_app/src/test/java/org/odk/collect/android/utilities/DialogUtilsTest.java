package org.odk.collect.android.utilities;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class DialogUtilsTest {

    @Test
    public void show_onlyEverOpensOneDialog() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        DialogFragment dialog1 = new DialogFragment();
        DialogUtils.showIfNotShowing(dialog1, fragmentManager);

        DialogFragment dialog2 = new DialogFragment();
        DialogUtils.showIfNotShowing(dialog2, fragmentManager);

        assertThat(fragmentManager.getFragments().size(), equalTo(1));
        assertThat(fragmentManager.getFragments().get(0), equalTo(dialog1));
    }
}