package org.odk.collect.android.architecture;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
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
 * @param <V> The MVVMViewModel subclass this Activity should load.
 *
 */
public abstract class MVVMActivity<V extends MVVMViewModel>
        extends AppCompatActivity {

    @Nullable
    private V viewModel;

    @Nullable
    private Unbinder unbinder;

    @Inject
    @Nullable
    public ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        AndroidInjection.inject(this);
        unbinder = ButterKnife.bind(this);

        viewModel = fetchViewModel();
        viewModel.create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }

        viewModelFactory = null;
        viewModel = null;
    }

    @NonNull
    public V getViewModel() {
        if (viewModel == null) {
            viewModel = fetchViewModel();
            viewModel.create();
        }

        return viewModel;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @NonNull
    protected abstract Class<V> getViewModelClass();

    private ViewModelProvider getViewModelProvider() {
        return viewModelFactory != null
                ? ViewModelProviders.of(this, viewModelFactory)
                : ViewModelProviders.of(this);
    }

    private V fetchViewModel() {
        return getViewModelProvider().get(getViewModelClass());
    }
}
