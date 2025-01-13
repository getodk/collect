package org.odk.collect.android.formentry.repeats;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.testshared.RobolectricHelpers;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AndroidJUnit4.class)
public class DeleteRepeatDialogFragmentTest {

    private TestActivity activity;
    private FragmentManager fragmentManager;
    private DeleteRepeatDialogFragment dialogFragment;

    private final FormController formController = mock(FormController.class, RETURNS_MOCKS);

    @Before
    public void setup() {
        FormEntryViewModel formEntryViewModel = mock(FormEntryViewModel.class);

        when(formEntryViewModel.getFormController()).thenReturn(formController);
        when(formController.getLastRepeatedGroupName()).thenReturn("blah");
        when(formController.getLastRepeatedGroupRepeatCount()).thenReturn(0);

        activity = CollectHelpers.createThemedActivity(TestActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new DeleteRepeatDialogFragment(new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
                return (T) formEntryViewModel;
            }
        });
    }

    @Test
    public void dialogIsNotCancellable() {
        launchDialog();
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(false));
    }

    @Test
    public void shouldShowCorrectMessage() {
        AlertDialog dialog = launchDialog();
        String message = ((TextView) dialog.findViewById(android.R.id.message)).getText().toString();

        assertThat(message, equalTo(ApplicationProvider.getApplicationContext().getString(org.odk.collect.strings.R.string.delete_repeat_confirm, "blah (1)")));
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        AlertDialog dialog = launchDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();

        RobolectricHelpers.runLooper();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingRemoveGroup_shouldDismissTheDialog() {
        AlertDialog dialog = launchDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        RobolectricHelpers.runLooper();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingRemoveGroup_setsResult() {
        AlertDialog dialog = launchDialog();

        FragmentResultCapturer fragmentResultCapturer = new FragmentResultCapturer();
        activity.getSupportFragmentManager().setFragmentResultListener(DeleteRepeatDialogFragment.REQUEST_DELETE_REPEAT, activity, fragmentResultCapturer);

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        RobolectricHelpers.runLooper();
        assertThat(fragmentResultCapturer.result, notNullValue());
    }

    @Test
    public void clickingRemoveGroup_callsDeleteRepeat() {
        AlertDialog dialog = launchDialog();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        RobolectricHelpers.runLooper();
        verify(formController).deleteRepeat();
    }

    private AlertDialog launchDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }

    public static class TestActivity extends FragmentActivity {

    }

    private static class FragmentResultCapturer implements FragmentResultListener {

        public Bundle result;

        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
            this.result = result;
        }
    }
}
