package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.ScreenContext;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class NumberPickerDialogTest {
    private TestRangePickerWidgetActivity activity;
    private FragmentManager fragmentManager;
    private NumberPickerDialog dialogFragment;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(TestRangePickerWidgetActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new NumberPickerDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(NumberPickerDialog.DISPLAYED_VALUES, new String[] {"5", "4", "3", "2", "1"});
        bundle.putInt(NumberPickerDialog.PROGRESS, 2);
        dialogFragment.setArguments(bundle);
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "tag");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void shouldShowCorrectButtons() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getText(), equalTo(activity.getString(R.string.ok)));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(), equalTo(activity.getString(R.string.cancel)));
    }

    @Test
    public void clickingOk_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void shouldShowCorrectNumberPicker() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);

        assertThat(numberPicker.getMinValue(), equalTo(0));
        assertThat(numberPicker.getMaxValue(), equalTo(4));
        assertThat(numberPicker.getDisplayedValues(), arrayContainingInAnyOrder("1", "2", "3", "4", "5"));
        assertThat(numberPicker.getValue(), equalTo(2));
    }

    @Test
    public void clickingOk_shouldUpdateCorrectValueInActivity() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);
        numberPicker.setValue(4);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        assertThat(activity.progress, equalTo(4));
    }

    public static class TestRangePickerWidgetActivity extends AppCompatActivity implements
            ScreenContext, NumberPickerDialog.NumberPickerListener {
        public int progress;

        @Override
        public FragmentActivity getActivity() {
            return this;
        }

        @Override
        public LifecycleOwner getViewLifecycle() {
            return this;
        }

        @Override
        public void onNumberPickerValueSelected(Integer value) {
            progress = value;
        }
    }
}
