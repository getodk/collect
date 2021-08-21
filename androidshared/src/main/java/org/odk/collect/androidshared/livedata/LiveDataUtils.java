package org.odk.collect.androidshared.livedata;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.Triple;

public class LiveDataUtils {

    private LiveDataUtils() {

    }

    public static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    public static <T, U, V> LiveData<Triple<T, U, V>> zip3(LiveData<T> one, LiveData<U> two, LiveData<V> three) {
        return new Zipped3LiveData<>(one, two, three);
    }

    public static <T, U, V, W> LiveData<Quad<T, U, V, W>> zip4(LiveData<T> one, LiveData<U> two, LiveData<V> three, LiveData<W> four) {
        return new Zipped4LiveData<>(one, two, three, four);
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

    private static class Zipped4LiveData<T, U, V, W> extends MediatorLiveData<Quad<T, U, V, W>> {

        private T lastOne;
        private U lastTwo;
        private V lastThree;
        private W lastFour;

        private Zipped4LiveData(LiveData<T> one, LiveData<U> two, LiveData<V> three, LiveData<W> four) {
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

            addSource(four, w -> {
                lastFour = w;
                update();
            });

            lastOne = one.getValue();
            lastTwo = two.getValue();
            lastThree = three.getValue();
            lastFour = four.getValue();
            setValue(new Quad<>(lastOne, lastTwo, lastThree, lastFour));
        }

        private void update() {
            if (getValue() != null) {
                setValue(new Quad<>(lastOne, lastTwo, lastThree, lastFour));
            }
        }
    }

    public static class Quad<T, U, V, W> {

        public final T first;
        public final U second;
        public final V third;
        public final W fourth;

        public Quad(T first, U second, V third, W fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }
    }
}
