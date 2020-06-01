package org.odk.collect.utilities;

/**
 * Polyfill for {@link java.util.function.Supplier}. Can be removed after min is API 24.
 */
public interface Supplier<T> {

    T get();
}

