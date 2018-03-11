package org.odk.collect.android.architecture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A ViewModel (or Presenter, if you prefer) with a simplified lifecycle.
 */
public abstract class ViewModel {

    private boolean wasCreated = false;
    private boolean wasDestroyed = false;

    final synchronized void create(@Nullable Bundle savedState) {
        if (wasCreated || wasDestroyed) {
            return;
        }

        onCreate();
        if (savedState != null) {
            onRestoreState(savedState);
        }

        wasCreated = true;
    }

    final synchronized void restoreState(@NonNull Bundle restoreState) {
        onRestoreState(restoreState);
    }

    final synchronized void saveState(@NonNull Bundle savedState) {
        onSaveState(savedState);
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
    protected void onRestoreState(@NonNull Bundle restoreState) {

    }

    @OverridingMethodsMustInvokeSuper
    protected void onSaveState(@NonNull Bundle savedState) {

    }

    @OverridingMethodsMustInvokeSuper
    protected void onDestroy() {

    }
}
