package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArrayUtilsTest {

    @Test
    public void toPrimitive() throws Exception {
        Long[] object = new Long[] {1L, 2L, 3L};
        long[] primitive = new long[] {1, 2, 3};
        assertArrayEquals(primitive, ArrayUtils.toPrimitive(object));

        long[] emptyLongPrimitive = new long[0];
        assertArrayEquals("toPrimitive(null) did not produce an empty long array", emptyLongPrimitive, ArrayUtils.toPrimitive(null));
    }

    @Test
    public void nullToPrimitive() {
        try {
            Long[] object = new Long[] {1L, null, 3L, 4L, 5L};
            ArrayUtils.toPrimitive(object);
            Assert.fail("NullPointer Exception was expected!");
        } catch (Exception e) {
            assertEquals(true, e instanceof NullPointerException);
        }

        try {
            Long[] objectStartingWithnull = new Long[] {null, 2L, 3L};
            ArrayUtils.toPrimitive(objectStartingWithnull);
            Assert.fail("NullPointer Exception was expected!");
        } catch (Exception e) {
            assertEquals(true, e instanceof NullPointerException);
        }

        try {
            Long[] objectEndingWithnull = new Long[] {1L, 2L, null};
            ArrayUtils.toPrimitive(objectEndingWithnull);
            Assert.fail("NullPointer Exception was expected!");
        } catch (Exception e) {
            assertEquals(true, e instanceof NullPointerException);
        }
    }

    @Test
    public void toObject() throws Exception {
        Long[] object = new Long[] {1L, 2L, 3L};
        long[] primitive = new long[] {1, 2, 3};
        assertArrayEquals(object, ArrayUtils.toObject(primitive));

        Long[] emptyLongObject = new Long[0];
        assertArrayEquals(emptyLongObject, ArrayUtils.toObject(null));
    }
}