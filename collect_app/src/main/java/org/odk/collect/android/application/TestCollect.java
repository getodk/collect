package org.odk.collect.android.application;

import com.squareup.leakcanary.RefWatcher;

public class TestCollect extends Collect {
    @Override
    protected RefWatcher setupLeakCanary() {
        // No leakcanary in unit tests.
        return RefWatcher.DISABLED;
    }
}