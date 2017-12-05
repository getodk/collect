package org.odk.collect.android.architecture;

import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A {@link ViewModel} subclass that provides an 'onCreate' method for simplifying
 * subscription timing.
 */
public class MVVMViewModel extends ViewModel {

    private boolean wasCreated = false;

    final synchronized void create(@Nullable Bundle parameters) {
        if (wasCreated) {
            return;
        }

        onCreate(parameters);
        wasCreated = true;
    }

    @OverridingMethodsMustInvokeSuper
    protected void onCreate(@Nullable Bundle parameters) {

    }
}
