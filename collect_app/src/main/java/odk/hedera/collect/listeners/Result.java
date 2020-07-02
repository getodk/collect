package odk.hedera.collect.listeners;

/**
 * Generic Result interface that returns a result of type T
 * from another thread or function.
 *
 */
public interface Result<T> {
    void onComplete(T result);
}
