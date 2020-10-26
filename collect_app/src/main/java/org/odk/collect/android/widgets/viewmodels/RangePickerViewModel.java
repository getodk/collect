package org.odk.collect.android.widgets.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;

public class RangePickerViewModel extends ViewModel {
    private String stringAnswer = "";

    private final MutableLiveData<Integer> numberPickerValue = new MutableLiveData<>();

    public LiveData<Integer> getNumberPickerValue() {
        return numberPickerValue;
    }

    public void setNumberPickerValue(int numberPickerValue) {
        this.numberPickerValue.postValue(numberPickerValue);
    }

    public int getNumberPickerProgress(BigDecimal rangeStart, BigDecimal rangeStep, BigDecimal rangeEnd, int value) {
        BigDecimal actualValue;
        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
        BigDecimal multiply = new BigDecimal(elementCount - value).multiply(rangeStep);

        if (rangeStart.compareTo(rangeEnd) < 0) {
            actualValue = rangeStart.add(multiply);
        } else {
            actualValue = rangeStart.subtract(multiply);
        }
        setStringAnswer(String.valueOf(actualValue));

        return actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
    }

    public String getStringAnswer() {
        return stringAnswer;
    }

    private void setStringAnswer(String stringAnswer) {
        this.stringAnswer = stringAnswer;
    }
}
