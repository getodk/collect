package org.odk.collect.android.architecture;


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
 * Create a new {@link ViewModel} subclass, override getViewModelClass() to
 * return the subclass you've created, and you'll have access to a persistent
 * VM in your onCreate thanks to Dagger.
 *
 * @param <V> The ViewModel subclass this Activity should load.
 *
 */
public abstract class ViewModelActivity<V extends ViewModel>
        extends InjectableActivity {

    @Inject
    protected V viewModel;

    @Nullable
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

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

        viewModel.destroy();
        viewModel = null;
    }

    @NonNull
    public V getViewModel() {
        return viewModel;
    }

    @LayoutRes
    protected abstract int getLayoutId();
}
