package org.odk.collect.android.widgets.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RangePickerViewModel extends ViewModel {
    private final MutableLiveData<Integer> numberPickerValue = new MutableLiveData<>();

    private String[] displayedValuesForNumberPicker;
    private Integer progress;

    public LiveData<Integer> getNumberPickerValue() {
        return numberPickerValue;
    }

    public void setNumberPickerValue(Integer numberPickerValue) {
        this.numberPickerValue.postValue(numberPickerValue);
    }

    public String[] getDisplayedValuesForNumberPicker() {
        return displayedValuesForNumberPicker.clone();
    }

    public void setDisplayedValuesForNumberPicker(String[] displayedValuesForNumberPicker) {
        this.displayedValuesForNumberPicker = displayedValuesForNumberPicker;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}
