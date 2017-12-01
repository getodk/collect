package org.odk.collect.android.architecture;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * A new Activity base class that uses Dagger to bootstrap VM creation.
 *
 * Create a new {@link MVVMViewModel} subclass, override getViewModelClass() to
 * return the subclass you've created, and you'll have access to a persistent
 * VM in your onCreate thanks to Dagger.
 *
 * Also handles subclass and Fragment injection, just add {@link Inject} fields
 * to your Activity and have any Fragments you want injected implement the
 * {@link org.odk.collect.android.injection.Injectable} interface.
 *
 * @param <VM>
 */
public abstract class MVVMActivity<VM extends MVVMViewModel>
        extends AppCompatActivity {

    @Nullable
    private VM viewModel;

    @Inject
    @Nullable
    public ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        AndroidInjection.inject(this);

        viewModel = fetchViewModel();
        viewModel.create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewModelFactory = null;
        viewModel = null;
    }

    @NonNull
    public VM getViewModel() {
        if (viewModel == null) {
            viewModel = fetchViewModel();
            viewModel.create();
        }

        return viewModel;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @NonNull
    protected abstract Class<VM> getViewModelClass();

    private ViewModelProvider getViewModelProvider() {
        return viewModelFactory != null
                ? ViewModelProviders.of(this, viewModelFactory)
                : ViewModelProviders.of(this);
    }

    private VM fetchViewModel() {
        return getViewModelProvider().get(getViewModelClass());
    }
}
