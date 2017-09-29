package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Date;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

public class EthiopianDateWidget extends AbstractDateWidget {

    private EthiopianDatePickerDialog ethiopianDatePickerDialog;

    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void createWidget() {
        super.createWidget();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ethiopianDatePickerDialog = EthiopianDatePickerDialog.newInstance(getId(), !nullAnswer, day, month, year, calendarMode);
                ethiopianDatePickerDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
            }
        });
    }

    @Override
    protected void setDateLabel() {
        String ethiopianDate = getEthiopianDateLabel(new DateTime(((Date) getAnswer().getValue()).getTime()), getContext());
        String gregorianDate = DateTimeUtils.getDateTimeBasedOnUserLocale((Date) getAnswer().getValue(), getPrompt().getQuestion().getAppearanceAttr(), false);

        dateTextView.setText(String.format(getContext().getString(R.string.ethiopian_date), ethiopianDate, gregorianDate));
    }

    private String getEthiopianDateLabel(DateTime dateTime, Context context) {
        DateTime ethiopianDate = dateTime.withChronology(EthiopicChronology.getInstance());
        String day = calendarMode.equals(CalendarMode.FULL_DATE) ? String.valueOf(ethiopianDate.getDayOfMonth()) +" " : "";
        String month = !calendarMode.equals(CalendarMode.YEAR) ? context.getResources().getStringArray(R.array.ethiopian_months)[ethiopianDate.getMonthOfYear() - 1] + " " : "";

        return day + month + ethiopianDate.getYear();
    }
}
