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
    private final MutableLiveData<LocalDateTime> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<DateTime> selectedTime = new MutableLiveData<>();

    private final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) ->
            setSelectedDate(year, monthOfYear, dayOfMonth);

    private final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minuteOfHour) -> {
        view.clearFocus();
        hourOfDay = view.getCurrentHour();
        minuteOfHour = view.getCurrentMinute();
        setSelectedTime(hourOfDay, minuteOfHour);
    };

    private LocalDateTime localDateTime;
    private DatePickerDetails datePickerDetails;
    private int dialogTheme;

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

    public DatePickerDialog.OnDateSetListener getDateSetListener() {
        return dateSetListener;
    }

    public TimePickerDialog.OnTimeSetListener getTimeSetListener() {
        return timeSetListener;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public DatePickerDetails getDatePickerDetails() {
        return datePickerDetails;
    }

    public void setDatePickerDetails(DatePickerDetails datePickerDetails) {
        this.datePickerDetails = datePickerDetails;
    }

    public int getDialogTheme() {
        return dialogTheme;
    }

    public void setDialogTheme(int dialogTheme) {
        this.dialogTheme = dialogTheme;
    }
}
