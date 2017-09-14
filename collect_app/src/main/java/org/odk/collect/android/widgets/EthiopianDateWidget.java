package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.EthiopianCalendarDialog;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Date;

public class EthiopianDateWidget extends DateWidget {

    private IAnswerData answerData;
    private EthiopianCalendarDialog ethiopianCalendarDialog;

    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        answerData = prompt.getAnswerValue();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ethiopianCalendarDialog = EthiopianCalendarDialog.newInstance(getId(), answerData != null, dayOfMonth, month, year);
                ethiopianCalendarDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), EthiopianCalendarDialog.ETHIOPIAN_DIALOG);
            }
        });
    }

    @Override
    public IAnswerData getAnswer() {
        return answerData;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        answerData = null;
    }

    @Override
    protected void createDatePickerDialog() {
        if (formEntryPrompt.getAnswerValue() == null) {
            clearAnswer();
        } else {
            DateTime dt = new DateTime(((Date) formEntryPrompt.getAnswerValue().getValue()).getTime());
            year = dt.getYear();
            month = dt.getMonthOfYear();
            dayOfMonth = dt.getDayOfMonth();
            setDateLabel();
        }
    }

    @Override
    protected void hideDayFieldIfNotInFormat() {
    }

    public void onDateChanged(IAnswerData data) {
        answerData = data;
        DateTime dt = new DateTime(((Date) data.getValue()).getTime());
        year = dt.getYear();
        month = dt.getMonthOfYear();
        dayOfMonth = dt.getDayOfMonth();
        setDateLabel();
    }

    public void setDateLabel() {
        nullAnswer = false;
        String ethiopianDate = DateTimeUtils.getEthiopianDate(new DateTime(((Date) answerData.getValue()).getTime()), getContext());
        String gregorianDate = DateTimeUtils.getDateTimeBasedOnUserLocale(
                (Date) getAnswer().getValue(), formEntryPrompt.getQuestion().getAppearanceAttr(), false);

        dateTextView.setText(ethiopianDate + " (" + gregorianDate + ")");
    }
}
