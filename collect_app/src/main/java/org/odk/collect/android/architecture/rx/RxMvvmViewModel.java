package org.odk.collect.android.architecture.rx;


import android.os.Bundle;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;

import org.odk.collect.android.architecture.MvvmActivity;
import org.odk.collect.android.architecture.MvvmViewModel;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;


/**
 * A {@link MvvmActivity} subclass that provides RxLifecycle
 * methods for binding {@link io.reactivex.Observable}'s to
 * the ViewModel lifecycle.
 */
public class RxMvvmViewModel extends MvvmViewModel {

    private final BehaviorSubject<Event> lifecycleSubject = BehaviorSubject.create();

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        lifecycleSubject.onNext(Event.ON_CREATE);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCleared() {
        super.onCleared();
        lifecycleSubject.onNext(Event.ON_CLEAR);
    }

    @NonNull
    protected <T> LifecycleTransformer<T> bindToLifecycle() {
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
