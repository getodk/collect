package org.odk.collect.utilities;

/**
 * An object that exposes the current time to its client. Useful for decoupling
 * objects from static methods such as {@link System#currentTimeMillis()}.
 *
 * @deprecated this is no longer needed now Android supports Java desugaring.
 * {@link java.util.function.Supplier} offers a standard alternative.
 */

@Deprecated
public interface Clock {

    long getCurrentTime();
}
