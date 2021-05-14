package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;

import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fragments.support.DialogFragmentHelpers;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.testshared.RobolectricHelpers;

import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MyanmarDatePickerDialogTest {
    private FragmentManager fragmentManager;
    private MyanmarDatePickerDialog dialogFragment;
    private DatePickerDetails datePickerDetails;
    private DialogFragmentHelpers.DatePickerTestActivity activity;

    @Before
    public void setup() {
        activity = CollectHelpers.createThemedActivity(DialogFragmentHelpers.DatePickerTestActivity.class);
        fragmentManager = activity.getSupportFragmentManager();

        dialogFragment = new MyanmarDatePickerDialog();
        datePickerDetails = DialogFragmentHelpers.setUpDatePickerDetails(DatePickerDetails.DatePickerType.MYANMAR);
        dialogFragment.setArguments(DialogFragmentHelpers.getDialogFragmentArguments(datePickerDetails));
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsCancellable(true);
    }

    @Test
    public void dialogShouldShowCorrectDate() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDate(1382, 1, 21, "21 ကဆုန် 1382 (2020May12)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forYearMode() {
        when(datePickerDetails.isYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDateForYearMode(1382, "1381 (2020)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forMonthMode() {
        when(datePickerDetails.isMonthYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDateForMonthMode(1382, 1, "တန်ခူး 1382 (2020Apr)");
    }

    @Test
    public void settingDateInDatePicker_changesDateShownInTextView() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogTextViewUpdatesDate("21 ကဆုန် 1382 (2020May12)");
    }

    @Test
    public void whenScreenIsRotated_dialogShouldRetainDateInDatePickerAndTextView() {
        DialogFragmentHelpers.assertDialogRetainsDateOnScreenRotation(dialogFragment, "12 တော်သလင်း 2020 (2658Sep30)");
    }

    @Test
    public void clickingOk_updatesDateInActivity() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDateUpdateInActivity(activity, 2658, 9, 30);
    }

    @Test
    public void clickingOk_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_POSITIVE);
    }

    @Test
    public void clickingCancel_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_NEGATIVE);
    }
}
