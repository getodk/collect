package org.odk.collect.android.architecture.rx;


import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;

import org.odk.collect.android.architecture.MVVMActivity;
import org.odk.collect.android.architecture.MVVMViewModel;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;


/**
 * A {@link MVVMActivity} subclass that provides RxLifecycle
 * methods for binding {@link io.reactivex.Observable}'s to
 * the ViewModel lifecycle.
 */
public class RxMVVMViewModel extends MVVMViewModel {

    private final BehaviorSubject<Event> lifecycleSubject = BehaviorSubject.create();

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCreate() {
        super.onCreate();
        lifecycleSubject.onNext(Event.ON_CREATE);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCleared() {
        super.onCleared();
        lifecycleSubject.onNext(Event.ON_CLEAR);
    }

    @NonNull
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycle.bind(lifecycleSubject, LIFECYCLE);
    }

    enum Event {
        ON_CREATE,
        ON_CLEAR
    }

    private static final Function<Event, Event> LIFECYCLE = lastEvent -> {
        switch (lastEvent) {
            case ON_CREATE:
                return Event.ON_CLEAR;

            case ON_CLEAR:
                throw new OutsideLifecycleException("Cannot bind to ViewModel lifecycle when outside of it.");

            default:
                throw new UnsupportedOperationException("Binding to " + lastEvent + " not yet implemented");
        }
    };
}
