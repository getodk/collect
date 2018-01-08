package org.odk.collect.android.utilities;

/**
 * @author James Knight
 */

public class Rx {
    public static <L, R> L takeLeft(L l, R r) {
        return l;
    }

    public static <L, R> R takeRight(L l, R r) {
        return r;
    }

    public static boolean isTrue(boolean b) {
        return b;
    }

    public static boolean isFalse(boolean b) {
        return !b;
    }

    public static boolean and(boolean a, boolean b) {
        return a && b;
    }

    public static boolean or(boolean a, boolean b) {
        return a || b;
    }
}
