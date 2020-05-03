package org.odk.collect.android.formentry;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.odk.collect.android.support.RobolectricHelpers.mockViewModelProvider;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class QuitFormDialogFragmentTest {

    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private QuitFormDialogFragment dialogFragment;

    private FormSaveViewModel viewModel;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new QuitFormDialogFragment();

        viewModel = mockViewModelProvider(activity, FormSaveViewModel.class).get(FormSaveViewModel.class);
    }

    @Test
    public void shouldShowCorrectView() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) dialogFragment.getDialog();

        assertNotNull(dialog);
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(GONE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getText(),
                equalTo(activity.getString(R.string.do_not_exit)));

        String title = dialogFragment.getTitle(viewModel);
        dialogFragment.setTitle();
        assertThat(shadowOf(dialogFragment.getDialog()).getTitle(), equalTo(title));
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "tag");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void restoringFragment_retainsTitle() {
        dialogFragment.show(fragmentManager, "tag");
        dialogFragment.setTitle();

        QuitFormDialogFragment restoredFragment = new QuitFormDialogFragment();
        restoredFragment.show(fragmentManager, "TAG");
        restoredFragment.setTitle();

        assertEquals(shadowOf(dialogFragment.getDialog()).getTitle(),
                shadowOf(restoredFragment.getDialog()).getTitle());
    }
}
