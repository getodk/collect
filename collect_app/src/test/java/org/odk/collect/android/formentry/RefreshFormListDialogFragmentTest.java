package org.odk.collect.android.formentry;


import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class RefreshFormListDialogFragmentTest {

    private FragmentManager fragmentManager;
    private RefreshFormListDialogFragment fragment;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        fragment = new RefreshFormListDialogFragment();
        fragment.listener = mock(RefreshFormListDialogFragment.RefreshFormListDialogFragmentListener.class);
    }

    @Test
    public void dialogIsNotCancellable() {
        fragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(fragment.getDialog()).isCancelable(), equalTo(false));
    }

    @Test
    public void clickingCancel_calls_onCancelFormLoading() {
        fragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        verify(fragment.listener).onCancelFormLoading();
    }
}
