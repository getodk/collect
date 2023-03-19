package org.odk.collect.android.widgets.support;

import java.util.ArrayList;
import java.util.Random;

public final class GeoWidgetHelpers {

    private GeoWidgetHelpers() {
    }

    public static double[] getRandomDoubleArray() {
        Random random = new Random();
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }

    public static String stringFromDoubleList() {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (double[] doubles : getRandomDoubleArrayList()) {
            if (!first) {
                b.append(';');
            }
            first = false;
            b.append(stringFromDoubles(doubles));
        }
        return b.toString();
    }

    private static ArrayList<double[]> getRandomDoubleArrayList() {
        Random random = new Random();
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
    }

    private static String stringFromDoubles(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < doubles.length; i++) {
            b.append(doubles[i]);
            if (i != doubles.length - 1) {
                b.append(' ');
            }
        }

        return b.toString();
    }
}
