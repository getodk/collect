package org.odk.collect.utilities;

import javax.annotation.Nullable;

/**
 * Based on https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/ which is not
 * available to Java.
 *
 * @deprecated use {@link org.odk.collect.shared.result.Result} instead
 */
@Deprecated
public class Result<T> {

    @Nullable
    private final T value;

    public Result(@Nullable T value) {
        this.value = value;
    }

    public T getOrNull() {
        return value;
    }

    public boolean isSuccess() {
        return value != null;
    }
}
