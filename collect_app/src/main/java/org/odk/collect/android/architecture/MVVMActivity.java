package org.odk.collect.android.architecture;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.activities.InjectableActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A new Activity base class that uses Dagger to bootstrap VM creation.
 *
 * Create a new {@link MVVMViewModel} subclass, override getViewModelClass() to
 * return the subclass you've created, and you'll have access to a persistent
 * VM in your onCreate thanks to Dagger.
 *
 * Also handles subclass and Fragment injection, just add {@link Inject} fields
 * to your Activity and have any Fragments you want injected implement the
 * {@link Injectable} interface.
 *
 * @param <V> The MVVMViewModel subclass this Activity should load.
 *
 */
public abstract class MVVMActivity<V extends MVVMViewModel>
        extends InjectableActivity {

    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    @Nullable
    private V viewModel;

    @Nullable
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());
        viewModel.create();

        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }

        viewModel = null;
        viewModelFactory = null;
    }

    @NonNull
    public V getViewModel() {
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());
            viewModel.create();
        }

        return viewModel;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @NonNull
    protected abstract Class<V> getViewModelClass();
}
