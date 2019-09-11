package org.odk.collect.android.support;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class LiveDataTester {

    private final FakeLifecycleOwner owner = new FakeLifecycleOwner();

    public <T> LiveData<T> activate(LiveData<T> liveData) {
        liveData.observe(owner, (Observer<Object>) any -> { });
        return liveData;
    }

    public void teardown() {
        owner.destroy();
    }
}
