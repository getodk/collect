package org.odk.collect.android.formentry.repeats;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.DialogInterface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormSessionRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.async.Scheduler;
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
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Scheduler scheduler, FormSessionRepository formSessionRepository) {
                return new FormEntryViewModel.Factory(null, null, null) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formEntryViewModel;
                    }
                };
            }
        });

        when(formEntryViewModel.getFormController()).thenReturn(formController);
        when(formController.getLastRepeatedGroupName()).thenReturn("blah");
        when(formController.getLastRepeatedGroupRepeatCount()).thenReturn(0);

        activity = CollectHelpers.createThemedActivity(TestActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new DeleteRepeatDialogFragment();
    }

    @Test
    public void fragmentActivityShouldImplementDeleteRepeatDialogCallback() {
        launchDialog();
        assertThat(dialogFragment.getActivity(), instanceOf(DeleteRepeatDialogFragment.DeleteRepeatDialogCallback.class));
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

        assertThat(message, equalTo(ApplicationProvider.getApplicationContext().getString(R.string.delete_repeat_confirm, "blah (1)")));
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
    public void clickingRemoveGroup_callsDeleteGroup() {
        AlertDialog dialog = launchDialog();
        assertThat(activity.deleteGroupCalled, equalTo(false));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        RobolectricHelpers.runLooper();
        assertThat(activity.deleteGroupCalled, equalTo(true));
        verify(formController).deleteRepeat();
    }

    private AlertDialog launchDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }

    public static class TestActivity extends FragmentActivity implements DeleteRepeatDialogFragment.DeleteRepeatDialogCallback {

        private boolean deleteGroupCalled;

        TestActivity() {
        }

        @Override
        public void deleteGroup() {
            deleteGroupCalled = true;
        }
    }
}
