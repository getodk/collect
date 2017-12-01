package org.odk.collect.android.injection.architecture;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentActivity;

import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

@ActivityScope
public class ActivityViewModelProvider {

    private final ViewModelProvider viewModelProvider;

    @Inject
    ActivityViewModelProvider(FragmentActivity activity,
                              ViewModelProvider.Factory factory) {
        viewModelProvider = ViewModelProviders.of(activity, factory);
    }

    public <T extends ViewModel> T get(Class<T> viewModelClass) {
        return viewModelProvider.get(viewModelClass);
    }
}
