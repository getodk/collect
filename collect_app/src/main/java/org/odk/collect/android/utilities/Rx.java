package org.odk.collect.android.utilities;

/**
 * @author James Knight
 */

public class Rx {
    public static <T> T id(T t) { return t; }

    public static <L, R> R takeRight(L l, R r) {
        return r;
    }

    public static Object toEvent(Object... objects) {
        return new Object();
    }

    public static boolean isTrue(boolean b) {
        return b;
    }

    public static boolean isFalse(boolean b) {
        return !b;
    }

    public static boolean not(boolean b) {
        return !b;
    }

    public static boolean and(boolean a, boolean b) {
        return a && b;
    }

    public static boolean or(boolean a, boolean b) {
        return a || b;
    }

    public static boolean xor(boolean a, boolean b) {
        return a ^ b;
    }

    public static boolean nand(boolean a, boolean b) {
        return not(and(a, b));
    }

    public static boolean nor(boolean a, boolean b) {
        return not(or(a, b));
    }

    public static void noop(Object... vars) {}
}
