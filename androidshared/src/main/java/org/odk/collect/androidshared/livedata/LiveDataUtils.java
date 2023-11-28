package org.odk.collect.androidshared.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static <T> void observeUntilNotNull(LiveData<T> liveData, Consumer<@NotNull T> consumer) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                if (value != null) {
                    consumer.accept(value);
                    liveData.removeObserver(this);
                }
            }
        };

        liveData.observeForever(observer);
    }

    public static <T> LiveData<T> liveDataOf(T value) {
        return new MutableLiveData<>(value);
    }

    public static <T, U, V> LiveData<Triple<T, U, V>> zip3(LiveData<T> one, LiveData<U> two, LiveData<V> three) {
        return new ZippedLiveData<>(
                new LiveData[]{one, two, three},
                values -> new Triple<>((T) values[0], (U) values[1], (V) values[2])
        );
    }

    public static <T, U, V, W> LiveData<Quad<T, U, V, W>> zip4(LiveData<T> one, LiveData<U> two, LiveData<V> three, LiveData<W> four) {
        return new ZippedLiveData<>(
                new LiveData[]{one, two, three, four},
                values -> new Quad<>((T) values[0], (U) values[1], (V) values[2], (W) values[3])
        );
    }

    public static <T, U> LiveData<U> asyncMap(Scheduler scheduler, LiveData<T> liveData, Function<T, U> func) {
        MediatorLiveData<U> mediator = new MediatorLiveData<>();
        mediator.addSource(liveData, value -> {
            scheduler.immediate(() -> func.apply(value), mediator::setValue);
        });

        return mediator;
    }

    private abstract static class DeferrableUpdateMediatorLiveData<T> extends MediatorLiveData<T> {

        private final int sources;
        private final Set<Integer> registeredSources = new HashSet<>();
        private int sourceCounter;

        DeferrableUpdateMediatorLiveData(int sources) {
            this.sources = sources;
        }

        public <S> void addDeferredSource(@NonNull LiveData<S> source, @NonNull Observer<? super S> onChanged) {
            addSource(source, s -> {
                registeredSources.add(sourceCounter++);
                onChanged.onChanged(s);
            });
        }

        /**
         * The value of the this {@link LiveData} will only be set once the `onChanged` for each
         * source (other than the last) added via
         * {@link DeferrableUpdateMediatorLiveData#addDeferredSource(LiveData, Observer)} has been
         * called once. This prevents unneeded early  calls to {@link LiveData#setValue(Object)}
         * when building up the initial state of a {@link MediatorLiveData} with many sources.
         */
        public void deferredSetValue(T value) {
            if (registeredSources.size() >= sources) {
                super.setValue(value);
            }
        }
    }

    private static class ZippedLiveData<T> extends DeferrableUpdateMediatorLiveData<T> {

        private final Object[] values;
        private final Function<Object[], T> map;

        ZippedLiveData(LiveData<?>[] sources, Function<Object[], T> map) {
            super(sources.length);
            this.map = map;
            values = new Object[sources.length];

            for (int i = 0; i < sources.length; i++) {
                int index = i;
                addDeferredSource(sources[i], value -> {
                    values[index] = value;
                    update();
                });
            }
        }

        private void update() {
            deferredSetValue(map.apply(values));
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
