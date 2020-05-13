package org.odk.collect.android.geo;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

        dialogFragment.callback = mock(SettingsDialogFragment.SettingsDialogCallback.class);
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

    @Test
    public void clickingStart_shouldCallStartInputMethod() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        verify(dialogFragment.callback, times(1)).startInput();
    }

    @Test
    public void selectingAutomaticMode_shouldDisplayIntervalAndAccuracyOptions() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
        View autoOptions = dialog.findViewById(R.id.auto_options);
        assertThat(autoOptions.getVisibility(), equalTo(GONE));

        radioGroup.check(R.id.automatic_mode);
        fragmentManager.executePendingTransactions();
        assertThat(autoOptions.getVisibility(), equalTo(VISIBLE));
    }

    @Test
    public void retainsIntervalAndSpinnerValueOnScreenOrientation() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
        Spinner autoInterval = dialog.findViewById(R.id.auto_interval);
        Spinner accuracyThreshold = dialog.findViewById(R.id.accuracy_threshold);

        radioGroup.check(R.id.automatic_mode);
        autoInterval.setSelection(2);
        accuracyThreshold.setSelection(2);

        assertThat(activity.get().getResources().getConfiguration().orientation, equalTo(Configuration.ORIENTATION_PORTRAIT));
        RuntimeEnvironment.setQualifiers("+land");
        activity.configurationChange();
        assertThat(activity.get().getResources().getConfiguration().orientation, equalTo(Configuration.ORIENTATION_LANDSCAPE));

        SettingsDialogFragment restoredFragment = (SettingsDialogFragment) activity.get().getSupportFragmentManager().findFragmentByTag("TAG");
        AlertDialog restoredDialog = (AlertDialog) restoredFragment.getDialog();

        assertThat(((RadioGroup) restoredDialog.findViewById(R.id.radio_group)).getCheckedRadioButtonId(), equalTo(R.id.automatic_mode));
        assertThat(((Spinner) restoredDialog.findViewById(R.id.auto_interval)).getSelectedItemPosition(), equalTo(2));
        assertThat(((Spinner) restoredDialog.findViewById(R.id.accuracy_threshold)).getSelectedItemPosition(), equalTo(2));
    }
}
