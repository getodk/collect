package org.odk.collect.android.widgets.viewmodels;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.widgets.utilities.FormControllerWaitingForDataRegistry;

public class DateTimeViewModel extends ViewModel {
    private final MutableLiveData<LocalDateTime> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDateTime> selectedTime = new MutableLiveData<>();

    private final FormControllerWaitingForDataRegistry waitingForDataRegistry = new FormControllerWaitingForDataRegistry();

    private final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        view.clearFocus();
        setSelectedDateTime(year, monthOfYear, dayOfMonth);
    };

    private final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minuteOfHour) -> {
        view.clearFocus();
        selectedTime.postValue(new LocalDateTime().withTime(hourOfDay, minuteOfHour, 0, 0));
    };

    public LiveData<LocalDateTime> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<LocalDateTime> getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedDateTime(int year, int month, int day) {
        this.selectedDate.postValue(new LocalDateTime().withDate(year, month + 1, day));
    }

    public DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        return dateSetListener;
    }

    public TimePickerDialog.OnTimeSetListener getOnTimeSetListener() {
        return timeSetListener;
    }

    public void setWidgetWaitingForData(FormIndex formIndex) {
        waitingForDataRegistry.waitForData(formIndex);
    }

    public boolean isWidgetWaitingForData(FormIndex formIndex) {
        if (waitingForDataRegistry.isWaitingForData(formIndex)) {
            waitingForDataRegistry.cancelWaitingForData();
            return true;
        }
        return false;
    }
}
