package org.odk.collect.android.widgets.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RangePickerViewModel extends ViewModel {
    private final MutableLiveData<Integer> numberPickerValue = new MutableLiveData<>();

    public LiveData<Integer> getNumberPickerValue() {
        return numberPickerValue;
    }

    public void setNumberPickerValue(int numberPickerValue) {
        this.numberPickerValue.postValue(numberPickerValue);
    }
}
