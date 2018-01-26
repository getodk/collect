package org.odk.collect.android.architecture.rx;


import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;

import org.odk.collect.android.architecture.ViewModel;
import org.odk.collect.android.architecture.ViewModelActivity;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;


/**
 * A {@link ViewModelActivity} subclass that provides RxLifecycle
 * methods for binding {@link io.reactivex.Observable}'s to
 * the ViewModel lifecycle.
 */
public class RxViewModel extends ViewModel {

    private final BehaviorSubject<Event> lifecycleSubject = BehaviorSubject.create();

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCreate() {
        super.onCreate();
        lifecycleSubject.onNext(Event.ON_CREATE);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onDestroy() {
        super.onDestroy();
        lifecycleSubject.onNext(Event.ON_DESTROY);
    }

    @NonNull
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycle.bind(lifecycleSubject, LIFECYCLE);
    }

    enum Event {
        ON_CREATE,
        ON_DESTROY
    }

    private static final Function<Event, Event> LIFECYCLE = lastEvent -> {
        switch (lastEvent) {
            case ON_CREATE:
                return Event.ON_DESTROY;

            case ON_DESTROY:
                throw new OutsideLifecycleException("Cannot bind to ViewModel lifecycle when outside of it.");

            default:
                throw new UnsupportedOperationException("Binding to " + lastEvent + " not yet implemented");
        }
    };
}
