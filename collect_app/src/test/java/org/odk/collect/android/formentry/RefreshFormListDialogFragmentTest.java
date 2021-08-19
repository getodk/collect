package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.fragmentstest.DialogFragmentTest;
import org.odk.collect.testshared.RobolectricHelpers;

@RunWith(AndroidJUnit4.class)
public class RefreshFormListDialogFragmentTest {

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<RefreshFormListDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(RefreshFormListDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }

    @Test
    public void clickingCancel_calls_onCancelFormLoading() {
        FragmentScenario<RefreshFormListDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(RefreshFormListDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.listener = mock(RefreshFormListDialogFragment.RefreshFormListDialogFragmentListener.class);

            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            RobolectricHelpers.runLooper();
            verify(fragment.listener).onCancelFormLoading();
        });
    }
}
