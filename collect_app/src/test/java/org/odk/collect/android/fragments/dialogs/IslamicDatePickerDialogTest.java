package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;

import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fragments.support.DialogFragmentHelpers;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.CollectHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class IslamicDatePickerDialogTest {
    private FragmentManager fragmentManager;
    private IslamicDatePickerDialog dialogFragment;
    private DatePickerDetails datePickerDetails;
    private DialogFragmentHelpers.DatePickerTestActivity activity;

    @Before
    public void setup() {
        activity = CollectHelpers.createThemedActivity(DialogFragmentHelpers.DatePickerTestActivity.class);
        fragmentManager = activity.getSupportFragmentManager();

        dialogFragment = new IslamicDatePickerDialog();
        datePickerDetails = DialogFragmentHelpers.setUpDatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC);
        dialogFragment.setArguments(DialogFragmentHelpers.getDialogFragmentArguments(datePickerDetails));
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDialogIsCancellable(true);
    }

    @Test
    public void dialogShouldShowCorrectDate() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDialogShowsCorrectDate(1441, 8, 19, "19 Ramadan 1441 (2020May12)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forYearMode() {
        when(datePickerDetails.isYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);
        dialogFragment.show(fragmentManager, "TAG");

        DialogFragmentHelpers.assertDialogShowsCorrectDateForYearMode(1441, "1441 (2019)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forMonthMode() {
        when(datePickerDetails.isMonthYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);
        dialogFragment.show(fragmentManager, "TAG");

        DialogFragmentHelpers.assertDialogShowsCorrectDateForMonthMode(1441, 8, "Ramadan 1441 (2020Apr)");
    }

    @Test
    public void settingDateInDatePicker_changesDateShownInTextView() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDialogTextViewUpdatesDate("19 Ramadan 1441 (2020May12)");
    }

    @Test
    public void whenScreenIsRotated_dialogShouldRetainDateInDatePickerAndTextView() {
        DialogFragmentHelpers.assertDialogRetainsDateOnScreenRotation(dialogFragment, "12 Jumada al-thani 2020 (2581Nov10)");
    }

    @Test
    public void clickingOk_updatesDateInActivity() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDateUpdateInActivity(activity, 2581, 11, 10);
    }

    @Test
    public void clickingOk_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_POSITIVE);
    }

    @Test
    public void clickingCancel_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_NEGATIVE);
    }
}
