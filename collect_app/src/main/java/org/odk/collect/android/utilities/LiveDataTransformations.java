package org.odk.collect.android.utilities;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public final class LiveDataTransformations {

    private LiveDataTransformations() {

    }

    // TODO
    public static <A, B> LiveData<Pair<A, B>> zip(LiveData<A> first, LiveData<B> second) {
        MediatorLiveData<Pair<A, B>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(first, (value) -> {
            mediatorLiveData.setValue(new Pair<>(first.getValue(), second.getValue()));
        });
        mediatorLiveData.addSource(second, (value) -> {
            mediatorLiveData.setValue(new Pair<>(first.getValue(), second.getValue()));
        });

        return mediatorLiveData;
    }
}
