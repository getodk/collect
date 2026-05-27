package org.odk.collect.androidshared.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.odk.collect.async.Cancellable;

import java.util.function.Consumer;
import java.util.function.Function;

import kotlin.Pair;
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

    public static <T, U> LiveData<Pair<T, U>> combine(LiveData<T> one, LiveData<U> two) {
        return new CombinedLiveData<>(
                new LiveData[]{one, two},
                values -> new Pair<>((T) values[0], (U) values[1])
        );
    }

    public static <T, U, V> LiveData<Triple<T, U, V>> combine3(LiveData<T> one, LiveData<U> two, LiveData<V> three) {
        return new CombinedLiveData<>(
                new LiveData[]{one, two, three},
                values -> new Triple<>((T) values[0], (U) values[1], (V) values[2])
        );
    }

    public static <T, U, V, W> LiveData<Quad<T, U, V, W>> combine4(LiveData<T> one, LiveData<U> two, LiveData<V> three, LiveData<W> four) {
        return new CombinedLiveData<>(
                new LiveData[]{one, two, three, four},
                values -> new Quad<>((T) values[0], (U) values[1], (V) values[2], (W) values[3])
        );
    }

    private static class CombinedLiveData<T> extends MediatorLiveData<T> {

        private final Object[] values;
        private final Function<Object[], T> map;
        private T lastEmitted;

        CombinedLiveData(LiveData<?>[] sources, Function<Object[], T> map) {
            this.map = map;
            values = new Object[sources.length];

            for (int i = 0; i < sources.length; i++) {
                int index = i;
                addSource(sources[i], value -> {
                    values[index] = value;
                    update();
                });
            }

            update();
        }

        private void update() {
            T newValue = map.apply(values);

            if (lastEmitted == null || !lastEmitted.equals(newValue)) {
                lastEmitted = newValue;
                setValue(newValue);
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
