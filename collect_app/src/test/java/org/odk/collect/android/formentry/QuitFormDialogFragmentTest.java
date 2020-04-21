package org.odk.collect.android.formentry;

import android.app.Dialog;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class QuitFormDialogFragmentTest {

    private FragmentManager fragmentManager;
    private QuitFormDialogFragment dialogFragment;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new QuitFormDialogFragment();
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "tag");

        fragmentManager.executePendingTransactions();
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void dismiss_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "tag");
        dialogFragment.dismiss();
        fragmentManager.executePendingTransactions();

        Dialog dialog = ShadowDialog.getLatestDialog();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }
}
