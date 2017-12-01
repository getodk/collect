package org.odk.collect.android.architecture;

import android.arch.lifecycle.ViewModel;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class MVVMViewModel extends ViewModel {

    private boolean wasCreated = false;

    final synchronized void create() {
        if (wasCreated) {
            return;
        }

        onCreate();
        wasCreated = true;
    }

    @OverridingMethodsMustInvokeSuper
    public void onCreate() {

    }
}
