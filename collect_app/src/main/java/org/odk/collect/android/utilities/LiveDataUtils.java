package org.odk.collect.android.utilities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import kotlin.Triple;

public class LiveDataUtils {

    private LiveDataUtils() {

    }

    public static <T, U, V> LiveData<Triple<T, U, V>> zip3(LiveData<T> one, LiveData<U> two, LiveData<V> three) {
        return new Zipped3LiveData<>(one, two, three);
    }

    private static class Zipped3LiveData<T, U, V> extends MediatorLiveData<Triple<T, U, V>> {

        private T lastOne;
        private U lastTwo;
        private V lastThree;

        private Zipped3LiveData(LiveData<T> one, LiveData<U> two, LiveData<V> three) {
            addSource(one, t -> {
                lastOne = t;
                update();
            });

            addSource(two, u -> {
                lastTwo = u;
                update();
            });

            addSource(three, v -> {
                lastThree = v;
                update();
            });

            lastOne = one.getValue();
            lastTwo = two.getValue();
            lastThree = three.getValue();
            setValue(new Triple<>(lastOne, lastTwo, lastThree));
        }

        private void update() {
            if (getValue() != null) {
                setValue(new Triple<>(lastOne, lastTwo, lastThree));
            }
        }
    }
}
