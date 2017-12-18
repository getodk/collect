package org.odk.collect.android.application;

import com.squareup.leakcanary.RefWatcher;

/**
 * @author James Knight
 */

public class TestCollect extends Collect {
    @Override
    protected RefWatcher setupLeakCanary() {
        // No leakcanary in unit tests.
        return RefWatcher.DISABLED;
    }
}