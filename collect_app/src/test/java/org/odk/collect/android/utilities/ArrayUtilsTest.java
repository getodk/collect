package org.odk.collect.android.utilities;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTest {
    @Test
    public void toPrimitive() throws Exception {
        Long[] object = new Long[] {1L, 2L, 3L};
        long[] primitive = new long[] {1, 2, 3};
        assertArrayEquals("toPrimitiveTest", primitive, ArrayUtils.toPrimitive(object));

        long[] emptyLongPrimitive = new long[0];
        assertArrayEquals("toPrimitiveNullTest", emptyLongPrimitive, ArrayUtils.toPrimitive(null));
    }

    @Test(expected = NullPointerException.class)
    public void nullToPrimitive() {
        Long[] object = new Long[] {1L, null, 3L, 4L, 5L};
        ArrayUtils.toPrimitive(object);

        Long[] objectStartingWithnull = new Long[] {null, 2L, 3L};
        ArrayUtils.toPrimitive(objectStartingWithnull);

        Long[] objectEndingWithnull = new Long[] {1L, 2L, null};
        ArrayUtils.toPrimitive(objectEndingWithnull);

    }

    @Test
    public void toObject() throws Exception {

        Long[] object = new Long[] {1L, 2L, 3L};
        long[] primitive = new long[] {1, 2, 3};
        assertArrayEquals("toObjectTest", object, ArrayUtils.toObject(primitive));

        Long[] emptyLongObject = new Long[0];
        assertArrayEquals("toObjectNullTest", emptyLongObject, ArrayUtils.toObject(null));
    }
}