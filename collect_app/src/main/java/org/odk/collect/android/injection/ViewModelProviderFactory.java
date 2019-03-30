package org.odk.collect.android.injection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.odk.collect.android.ui.formDownload.FormDownloadViewModel;
import org.odk.collect.android.utilities.rx.SchedulerProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ViewModelProviderFactory extends ViewModelProvider.NewInstanceFactory {

    private final SchedulerProvider schedulerProvider;

    @Inject
    public ViewModelProviderFactory(SchedulerProvider schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FormDownloadViewModel.class)) {
            //noinspection unchecked
            return (T) new FormDownloadViewModel(schedulerProvider);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
