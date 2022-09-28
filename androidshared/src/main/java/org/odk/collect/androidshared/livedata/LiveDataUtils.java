package org.odk.collect.androidshared.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.odk.collect.async.Cancellable;

import java.util.function.Consumer;

import kotlin.Triple;

public class LiveDataUtils {

    private LiveDataUtils() {

    }

    public static <T> Cancellable observe(LiveData<T> liveData, Consumer<T> consumer) {
        Observer<T> observer = value -> {
            if (value != null) {
                consumer.accept(value);
            }
        };

        liveData.observeForever(observer);
        return () -> {
            liveData.removeObserver(observer);
            return true;
        };
    }

    public static <T> LiveData<T> liveDataOf(T value) {
        return new MutableLiveData<>(value);
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
