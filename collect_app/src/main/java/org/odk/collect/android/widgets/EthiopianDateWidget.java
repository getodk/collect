package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.EthiopianCalendarDialog;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Date;

public class EthiopianDateWidget extends DateWidgetAbstract {

    private EthiopianCalendarDialog ethiopianCalendarDialog;

    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        if (formEntryPrompt.getAnswerValue() == null) {
            clearAnswer();
        } else {
            DateTime dt = new DateTime(((Date) formEntryPrompt.getAnswerValue().getValue()).getTime());
            year = dt.getYear();
            month = dt.getMonthOfYear();
            day = dt.getDayOfMonth();
            setDateLabel();
        }
    }

    @Override
    protected void createWidget() {
        super.createWidget();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ethiopianCalendarDialog = EthiopianCalendarDialog.newInstance(getId(), !nullAnswer, day, month, year);
                ethiopianCalendarDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), EthiopianCalendarDialog.ETHIOPIAN_DIALOG);
            }
        });
    }

    public void onDateChanged(int day, int month, int year) {
        nullAnswer = false;
        this.day = day;
        this.month = month;
        this.year = year;
        setDateLabel();
    }

    public void setDateLabel() {
        String ethiopianDate = DateTimeUtils.getEthiopianDate(new DateTime(((Date) getAnswer().getValue()).getTime()), getContext());
        String gregorianDate = DateTimeUtils.getDateTimeBasedOnUserLocale((Date) getAnswer().getValue(), null, false);

        dateTextView.setText(String.format(getContext().getString(R.string.ethiopian_date), ethiopianDate, gregorianDate));
    }
}
