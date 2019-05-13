package org.odk.collect.android.utilities;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.odk.collect.android.utilities.ArrayUtils.toPrimitive;
import static org.odk.collect.android.utilities.ArrayUtils.toObject;

public class ArrayUtilsTest {

    @Ignore
    @Test
    public void toPrimitiveCreatesPrimitiveLongArray() throws Exception {
        assertArrayEquals(new long[] {1, 2, 3, 4, 5}, toPrimitive(new Long[] {1L, 2L, 3L, 4L, 5L}));
    }

    @Ignore
    @Test
    public void nullToPrimitiveCreatesEmptyPrimitiveLongArray() throws Exception {
        assertArrayEquals(new long[0], toPrimitive(null));
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void arrayContainingNullCausesNpe() {
        toPrimitive(new Long[] {1L, null, 3L, 4L, 5L});
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void arrayStartingWithNullCausesNpe() {
        toPrimitive(new Long[] {null, 3L, 4L, 5L});
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void arrayEndingWithNullCausesNpe() {
        toPrimitive(new Long[] {1L, 3L, 4L, null});
    }

    @Ignore
    @Test
    public void toObjectCreatesLongArray() throws Exception {
        assertArrayEquals(new Long[] {1L, 2L, 3L, 4L, 5L}, toObject(new long[] {1, 2, 3, 4, 5}));
    }

    @Ignore
    @Test
    public void nullBecomesEmptyLongArray() throws Exception {
        assertArrayEquals(new Long[0], toObject(null));
    }
}
