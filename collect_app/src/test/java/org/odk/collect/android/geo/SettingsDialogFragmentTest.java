package org.odk.collect.android.geo;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

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
        dialogFragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void shouldShowCorrectButtons() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getText(),
                equalTo(activity.get().getString(R.string.start)));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(),
                equalTo(activity.get().getString(R.string.cancel)));
    }

    @Test
    public void clickingStart_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }
}
