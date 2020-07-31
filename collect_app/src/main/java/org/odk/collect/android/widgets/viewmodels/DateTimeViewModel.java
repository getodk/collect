package org.odk.collect.android.widgets.viewmodels;

import android.app.DatePickerDialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.widgets.utilities.FormControllerWaitingForDataRegistry;

import timber.log.Timber;

public class DateTimeViewModel extends ViewModel {
    private final MutableLiveData<LocalDateTime> selectedDateTime = new MutableLiveData<>();

    private final FormControllerWaitingForDataRegistry waitingForDataRegistry = new FormControllerWaitingForDataRegistry();

    private final DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
        view.clearFocus();
        Timber.d("Testing: data received in listener");
        selectedDateTime.postValue(new LocalDateTime().withDate(year, month + 1, dayOfMonth));
    };

    public LiveData<LocalDateTime> getSelectedDateTime() {
        return selectedDateTime;
    }

    public DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        return listener;
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
