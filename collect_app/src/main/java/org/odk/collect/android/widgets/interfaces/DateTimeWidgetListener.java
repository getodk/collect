package org.odk.collect.android.widgets.interfaces;

import android.content.Context;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.logic.DatePickerDetails;

public interface DateTimeWidgetListener {
    void displayDatePickerDialog(Context context, FormIndex formIndex, DatePickerDetails datePickerDetails, LocalDateTime selectedDate);

    void displayTimePickerDialog(Context context, LocalDateTime selectedTime);
}
