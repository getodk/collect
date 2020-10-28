package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.FormIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialogTest;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class DialogFragmentRangeDataRequesterTest {
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final FormIndex formIndex = mock(FormIndex.class);
    private final String[] sampleDisplayedValues = {"1", "2", "3", "4", "5"};

    private DialogFragmentRangeDataRequester dataRequester;
    private NumberPickerDialogTest.TestRangePickerWidgetActivity activity;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(NumberPickerDialogTest.TestRangePickerWidgetActivity.class);
        dataRequester = new DialogFragmentRangeDataRequester(activity, waitingForDataRegistry);
    }

    @Test
    public void requestRangePickerValue_showsNumberPickerValue_withCorrectArguments() {
        dataRequester.requestRangePickerValue(formIndex, sampleDisplayedValues, 2);
        NumberPickerDialog dialog = (NumberPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(NumberPickerDialog.class.getName());

        assertNotNull(dialog);
        assertThat((String[]) dialog.getArguments().getSerializable(NumberPickerDialog.DISPLAYED_VALUES),
                arrayContainingInAnyOrder("1", "2", "3", "4", "5"));
        assertThat(dialog.getArguments().getInt(NumberPickerDialog.PROGRESS), equalTo(2));
    }

    @Test
    public void requestRangePickerValue_setsWidgetWaitingForData() {
        dataRequester.requestRangePickerValue(formIndex, sampleDisplayedValues, 2);
        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }
}
