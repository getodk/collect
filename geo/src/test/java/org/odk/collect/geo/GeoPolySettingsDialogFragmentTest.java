package org.odk.collect.geo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.odk.collect.testshared.RobolectricHelpers;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AndroidJUnit4.class)
public class GeoPolySettingsDialogFragmentTest {

    private final int sampleId = R.id.automatic_mode;

    private FragmentManager fragmentManager;
    private GeoPolySettingsDialogFragment dialogFragment;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class, R.style.Theme_MaterialComponents);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new GeoPolySettingsDialogFragment();

        dialogFragment.callback = mock(GeoPolySettingsDialogFragment.SettingsDialogCallback.class);
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void clickingStart_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        RobolectricHelpers.runLooper();

        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        RobolectricHelpers.runLooper();

        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingStart_callsCorrectMethodsInCorrectOrder() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
        Spinner autoInterval = dialog.findViewById(R.id.auto_interval);
        Spinner accuracyThreshold = dialog.findViewById(R.id.accuracy_threshold);

        radioGroup.check(sampleId);
        autoInterval.setSelection(2);
        accuracyThreshold.setSelection(2);

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        RobolectricHelpers.runLooper();

        InOrder orderVerifier = Mockito.inOrder(dialogFragment.callback);
        orderVerifier.verify(dialogFragment.callback).updateRecordingMode(sampleId);
        orderVerifier.verify(dialogFragment.callback).setIntervalIndex(2);
        orderVerifier.verify(dialogFragment.callback).setAccuracyThresholdIndex(2);
        orderVerifier.verify(dialogFragment.callback).startInput();
    }

    @Test
    public void selectingAutomaticMode_displaysIntervalAndAccuracyOptions() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
        View autoOptions = dialog.findViewById(R.id.auto_options);
        assertThat(autoOptions.getVisibility(), equalTo(GONE));

        radioGroup.check(sampleId);
        assertThat(autoOptions.getVisibility(), equalTo(VISIBLE));
    }

    @Test
    public void notSelectingAutomaticMode_doesNotDisplayIntervalAndAccuracyOptions() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
        View autoOptions = dialog.findViewById(R.id.auto_options);

        radioGroup.check(R.id.manual_mode);
        assertThat(autoOptions.getVisibility(), equalTo(GONE));
    }

    @Test
    public void creatingDialog_showsCorrectView() {
        when(dialogFragment.callback.getCheckedId()).thenReturn(sampleId);
        when(dialogFragment.callback.getIntervalIndex()).thenReturn(2);
        when(dialogFragment.callback.getAccuracyThresholdIndex()).thenReturn(2);

        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        assertThat(dialog.findViewById(R.id.auto_options).getVisibility(), equalTo(VISIBLE));
        assertThat(((RadioGroup) dialog.findViewById(R.id.radio_group)).getCheckedRadioButtonId(), equalTo(sampleId));
        assertThat(((Spinner) dialog.findViewById(R.id.auto_interval)).getSelectedItemPosition(), equalTo(2));
        assertThat(((Spinner) dialog.findViewById(R.id.accuracy_threshold)).getSelectedItemPosition(), equalTo(2));
    }
}
