package org.odk.collect.android.widgets.support;

import android.os.Bundle;

import org.odk.collect.android.activities.GeoPolyActivity;

import java.util.ArrayList;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.LOCATION;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.READ_ONLY;

public final class GeoWidgetHelpers {

    private GeoWidgetHelpers() {
    }

    public static void assertGeoPointBundleArgumentEquals(Bundle bundle, double[] location, double accuracyThreshold, Boolean readOnly, Object draggableOnly) {
        assertThat(bundle.getDoubleArray(LOCATION), equalTo(location));
        assertThat(bundle.getDouble(ACCURACY_THRESHOLD), equalTo(accuracyThreshold));
        assertThat(bundle.getBoolean(READ_ONLY), equalTo(readOnly));
        assertThat(bundle.getBoolean(DRAGGABLE_ONLY), equalTo(draggableOnly));
    }

    public static void assertGeoPolyBundleArgumentEquals(Bundle bundle, String answer, GeoPolyActivity.OutputMode outputMode, boolean readOnly) {
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo(answer));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(outputMode));
        assertThat(bundle.getBoolean(READ_ONLY), equalTo(readOnly));
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
