package org.odk.collect.android.architecture.rx;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;

import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;

import org.odk.collect.android.architecture.MVVMActivity;

/**
 * A {@link MVVMActivity} subclass that provides RxLifecycle
 * methods for binding {@link io.reactivex.Observable}'s to
 * the Activity lifecycle.
 */
public abstract class RxMVVMActivity extends MVVMActivity {
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    private final LifecycleProvider<Lifecycle.Event> provider =
            AndroidLifecycle.createLifecycleProvider(this);

    protected <T> LifecycleTransformer<T> bindToLifecycle() {
        return provider.bindToLifecycle();
    }
}
