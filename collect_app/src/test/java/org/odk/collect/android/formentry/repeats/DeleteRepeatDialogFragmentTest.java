package org.odk.collect.android.formentry.repeats;

import android.content.DialogInterface;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.mockViewModelProvider;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class DeleteRepeatDialogFragmentTest {

    private FragmentManager fragmentManager;
    private DeleteRepeatDialogFragment dialogFragment;
    private FormEntryViewModel formEntryViewModel;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new DeleteRepeatDialogFragment();

        dialogFragment.callback = mock(DeleteRepeatDialogFragment.DeleteRepeatDialogCallback.class);
        formEntryViewModel = mockViewModelProvider(activity, FormEntryViewModel.class).get(FormEntryViewModel.class);
    }

    @Test
    public void dialogIsNotCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(false));
    }

    @Test
    public void shouldShowCorrectMessage() {
        when(formEntryViewModel.getLastRepeatedGroupName()).thenReturn("blah");
        when(formEntryViewModel.getLastRepeatedGroupRepeatCount()).thenReturn(0);
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        String message = ((TextView) dialog.findViewById(android.R.id.message)).getText().toString();

        assertThat(message, equalTo(RuntimeEnvironment.application.getString(R.string.delete_repeat_confirm, "blah (1)")));
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

    @Test
    public void clickingRemoveGroup_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingRemoveGroup_callsDeleteGroup() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        verify(dialogFragment.callback).deleteGroup();
    }

    @Test
    public void clickingCancel_callsOnCancelled() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        verify(dialogFragment.callback).onCancelled();
    }
}
