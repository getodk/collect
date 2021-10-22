package org.odk.collect.android.widgets.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY;
import static org.odk.collect.geo.Constants.EXTRA_READ_ONLY;
import static org.odk.collect.geo.GeoPointActivity.EXTRA_ACCURACY_THRESHOLD;

import android.os.Bundle;

import org.odk.collect.geo.GeoPolyActivity;
import org.odk.collect.geo.GeoPointMapActivity;

import java.util.ArrayList;
import java.util.Random;

public final class GeoWidgetHelpers {

    private GeoWidgetHelpers() {
    }

    public static void assertGeoPointBundleArgumentEquals(Bundle bundle, double[] location, double accuracyThreshold, Boolean readOnly, Object draggableOnly) {
        assertThat(bundle.getDoubleArray(GeoPointMapActivity.EXTRA_LOCATION), equalTo(location));
        assertThat(bundle.getDouble(EXTRA_ACCURACY_THRESHOLD), equalTo(accuracyThreshold));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(readOnly));
        assertThat(bundle.getBoolean(EXTRA_DRAGGABLE_ONLY), equalTo(draggableOnly));
    }

    public static void assertGeoPolyBundleArgumentEquals(Bundle bundle, String answer, GeoPolyActivity.OutputMode outputMode, boolean readOnly) {
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo(answer));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(outputMode));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(readOnly));
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
