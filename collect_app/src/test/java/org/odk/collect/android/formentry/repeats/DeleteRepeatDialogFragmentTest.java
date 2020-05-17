package org.odk.collect.android.formentry.repeats;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class DeleteRepeatDialogFragmentTest {

    private ActivityController<FragmentActivity> activity;
    private FragmentManager fragmentManager;
    private DeleteRepeatDialogFragment dialogFragment;

    @Before
    public void setup() {
        activity = RobolectricHelpers.buildThemedActivity(FragmentActivity.class);
        activity.setup();
        fragmentManager = activity.get().getSupportFragmentManager();
        dialogFragment = new DeleteRepeatDialogFragment();

        dialogFragment.formController = mock(FormController.class);
        dialogFragment.callback = mock(DeleteRepeatDialogFragment.DeleteRepeatDialogCallback.class);
    }

    @Test
    public void dialogIsNotCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        activity.get().finish();
        assertThat(dialog.isShowing(), equalTo(true));
    }

    @Test
    public void shouldShowCorrectMessage() {
        when(dialogFragment.formController.getLastRepeatedGroupName()).thenReturn("blah");
        when(dialogFragment.formController.getLastRepeatedGroupRepeatCount()).thenReturn(0);
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        String message = ((TextView) dialog.findViewById(android.R.id.message)).getText().toString();

        assertThat(message, equalTo(RuntimeEnvironment.application.getString(R.string.delete_repeat_confirm, "blah (1)")));
    }

    @Test
    public void shouldRetainMessageOnScreenRotation() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        String message = ((TextView) dialog.findViewById(android.R.id.message)).getText().toString();

        assertThat(activity.get().getResources().getConfiguration().orientation, equalTo(Configuration.ORIENTATION_PORTRAIT));
        RuntimeEnvironment.setQualifiers("+land");
        activity.configurationChange();
        assertThat(activity.get().getResources().getConfiguration().orientation, equalTo(Configuration.ORIENTATION_LANDSCAPE));

        DeleteRepeatDialogFragment restoredFragment = (DeleteRepeatDialogFragment) activity.get().getSupportFragmentManager().findFragmentByTag("TAG");
        AlertDialog restoredDialog = (AlertDialog) restoredFragment.getDialog();
        assertThat(((TextView) restoredDialog.findViewById(android.R.id.message)).getText().toString(), equalTo(message));
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
        verify(dialogFragment.callback, times(1)).deleteGroup();
    }

    @Test
    public void clickingCancel_callsOnCancelled() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        verify(dialogFragment.callback, times(1)).onCancelled();
    }
}
