package org.odk.collect.android.utilities;

public final class ArrayUtils {

    /**
     * An empty immutable {@code Long} array.
     */
    private static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

    /**
     * An empty immutable {@code long} array.
     */
    private static final long[] EMPTY_LONG_ARRAY = new long[0];

    private ArrayUtils() {

    }

    /**
     * <p>Converts an array of primitive longs to objects.</p>
     *
     * @param array a {@code long} array
     * @return a {@code Long} array
     */
    @SuppressWarnings("PMD.AvoidArrayLoops")
    public static Long[] toObject(long[] array) {
        if (array == null || array.length == 0) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        final Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * <p>Converts an array of object Longs to primitives.</p>
     *
     * @param array a {@code Long} array
     * @return a {@code long} array
     */
    @SuppressWarnings("PMD.AvoidArrayLoops")
    public static long[] toPrimitive(Long[] array) {
        if (array == null || array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }
}
