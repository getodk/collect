package org.odk.collect.android.ui.base;

import android.arch.lifecycle.ViewModel;

import org.odk.collect.android.utilities.rx.SchedulerProvider;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

public class BaseViewModel<N> extends ViewModel {

    private final CompositeDisposable compositeDisposable;
    private final SchedulerProvider schedulerProvider;

    private WeakReference<N> navigator;

    public BaseViewModel(SchedulerProvider schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }

    public CompositeDisposable getCompositeDisposable() {
        return compositeDisposable;
    }

    public N getNavigator() {
        return navigator.get();
    }

    public void setNavigator(N navigator) {
        this.navigator = new WeakReference<>(navigator);
    }

    public SchedulerProvider getSchedulerProvider() {
        return schedulerProvider;
    }
}
