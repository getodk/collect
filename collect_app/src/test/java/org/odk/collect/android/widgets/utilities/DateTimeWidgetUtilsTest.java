package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.COPTIC;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.ETHIOPIAN;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;

@RunWith(RobolectricTestRunner.class)
public class DateTimeWidgetUtilsTest {

    private FormEntryActivity activity;
    private FormEntryPrompt prompt;
    private QuestionDef questionDef;
    private DatePickerDetails datePickerDetails;
    private LocalDateTime date  = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);

    @Before
    public void setUp() {
        activity = RobolectricHelpers.createThemedActivity(FormEntryActivity.class);

        questionDef = mock(QuestionDef.class);
        datePickerDetails = mock(DatePickerDetails.class);
        prompt = promptWithQuestionDefAndAnswer(questionDef, null);
    }

    @Test
    public void showDatePickerDialog_showsFixedDatePickerDialog_whenDatePickerTypeIsNull() {
        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        FixedDatePickerDialog dialog = (FixedDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsEthiopianDatePickerDialog_whenDatePickerTypeIsEthiopian() {
        when(datePickerDetails.getDatePickerType()).thenReturn(ETHIOPIAN);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        CustomDatePickerDialog dialog = (CustomDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsCopticDatePickerDialog_whenDatePickerTypeIsCoptic() {
        when(datePickerDetails.getDatePickerType()).thenReturn(COPTIC);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        CopticDatePickerDialog dialog = (CopticDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }
}
