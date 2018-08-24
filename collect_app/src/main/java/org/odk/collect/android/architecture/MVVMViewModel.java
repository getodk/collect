package org.odk.collect.android.architecture;

import android.arch.lifecycle.ViewModel;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A {@link ViewModel} subclass that provides an 'onCreate' method for simplifying
 * subscription timing.
 */
public class MVVMViewModel extends ViewModel {

    private boolean wasCreated;

    final synchronized void create() {
        if (wasCreated) {
            return;
        }

        onCreate();
        wasCreated = true;
    }

    @OverridingMethodsMustInvokeSuper
    protected void onCreate() {

    }
}
