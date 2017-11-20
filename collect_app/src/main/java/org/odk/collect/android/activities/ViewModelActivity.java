package org.odk.collect.android.activities;


import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import org.odk.collect.android.BR;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.android.AndroidInjection;

public abstract class ViewModelActivity<VM extends ViewModel, VMF extends ViewModelProvider.Factory, DB extends ViewDataBinding>
        extends AppCompatActivity {

    @Nullable
    private VM viewModel;

    @Nullable
    private DB binding;

    @Inject
    @Nullable
    public VMF viewModelFactory;

    @Override
    protected void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        this.binding = createBinding();
        this.viewModel = createViewModel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;

        if (binding != null) {
            binding.unbind();
            binding = null;
        }
    }

    @NonNull
    public VM getViewModel() {
        if (viewModel == null) {
            viewModel = createViewModel();
        }

        return viewModel;
    }

    public DB getBinding() {
        if (binding == null) {
            binding = createBinding();
        }

        return binding;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @NonNull
    protected abstract Class<VM> getViewModelClass();

    private DB createBinding() {
        if (binding != null) {
            return binding;
        }

        return DataBindingUtil.setContentView(this, getLayoutId());
    }

    private VM createViewModel() {
        if (viewModel != null) {
            return viewModel;
        }


        Class<VM> viewModelClass = getViewModelClass();
        ViewModelProvider viewModelProvider = getProvider();

        VM viewModel = viewModelProvider.get(viewModelClass);

        ViewDataBinding binding = getBinding();
        binding.setVariable(BR.viewModel, viewModel);

        return viewModel;
    }

    private ViewModelProvider getProvider() {
        return viewModelFactory != null
                ? ViewModelProviders.of(this, viewModelFactory)
                : ViewModelProviders.of(this);
    }
}
