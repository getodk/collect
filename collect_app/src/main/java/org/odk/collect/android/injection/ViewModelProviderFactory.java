package org.odk.collect.android.injection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.odk.collect.android.ui.formdownload.FormDownloadRepository;
import org.odk.collect.android.ui.formdownload.FormDownloadViewModel;
import org.odk.collect.android.utilities.NetworkUtils;
import org.odk.collect.android.utilities.providers.BaseResourceProvider;
import org.odk.collect.android.utilities.rx.SchedulerProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ViewModelProviderFactory extends ViewModelProvider.NewInstanceFactory {

    @Inject
    NetworkUtils networkUtils;

    @Inject
    BaseResourceProvider resourceProvider;

    @Inject
    FormDownloadRepository downloadRepository;

    @Inject
    SchedulerProvider schedulerProvider;

    @Inject
    public ViewModelProviderFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FormDownloadViewModel.class)) {
            //noinspection unchecked
            return (T) new FormDownloadViewModel(schedulerProvider, networkUtils, resourceProvider, downloadRepository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
