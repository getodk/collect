package org.odk.collect.android.widgets.viewmodels;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

public class DateTimeViewModel extends ViewModel {
    public final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        setSelectedDate(year, monthOfYear, dayOfMonth);
    };

    public final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minuteOfHour) -> {
        view.clearFocus();
        hourOfDay = view.getCurrentHour();
        minuteOfHour = view.getCurrentMinute();
        setSelectedTime(hourOfDay, minuteOfHour);
    };

    public LocalDateTime localDateTime;
    public DatePickerDetails datePickerDetails;
    public int dialogTheme;

    private final MutableLiveData<LocalDateTime> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<DateTime> selectedTime = new MutableLiveData<>();

    public LiveData<LocalDateTime> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<DateTime> getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedDate(int year, int month, int day) {
        this.selectedDate.postValue(DateTimeUtils.getSelectedDate(new LocalDateTime().withDate(year, month + 1, day), LocalDateTime.now()));
    }

    public void setSelectedTime(int hourOfDay, int minuteOfHour) {
        selectedTime.postValue(new DateTime().withTime(hourOfDay, minuteOfHour, 0, 0));
    }
}
