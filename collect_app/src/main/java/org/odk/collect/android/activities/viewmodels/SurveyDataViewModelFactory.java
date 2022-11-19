package org.odk.collect.android.activities.viewmodels;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SurveyDataViewModelFactory implements ViewModelProvider.Factory {

    private final SharedPreferences sharedPreferences;

    public SurveyDataViewModelFactory(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SurveyDataViewModel(sharedPreferences);
    }
}
