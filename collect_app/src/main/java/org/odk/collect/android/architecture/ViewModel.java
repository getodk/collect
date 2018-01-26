package org.odk.collect.android.architecture;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A ViewModel (or Presenter, if you prefer) with a simplified lifecycle.
 */
public abstract class ViewModel {

    private boolean wasCreated = false;
    private boolean wasDestroyed = false;

    final synchronized void create() {
        if (wasCreated || wasDestroyed) {
            return;
        }

        onCreate();
        wasCreated = true;
    }

    final synchronized void destroy() {
        if (!wasCreated || wasDestroyed) {
            return;
        }

        onDestroy();
        wasDestroyed = false;
    }

    @OverridingMethodsMustInvokeSuper
    protected void onCreate() {

    }

    @OverridingMethodsMustInvokeSuper
    protected void onDestroy() {

    }
}
