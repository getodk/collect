package org.odk.collect.utilities;

/**
 * Polyfill for {@link java.util.function.Consumer}. Can be removed after min is API 24.
 */
public interface Consumer<T> {

    void accept(T t);
}
