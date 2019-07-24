package org.odk.collect.android.widgets;

import android.content.Intent;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.ShadowPlayServicesUtil;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_OSM;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BASEMAP_SOURCE;

/**
 * @author James Knight
 */

@Config(shadows = {ShadowPlayServicesUtil.class})
public class GeoTraceWidgetTest extends BinaryWidgetTest<GeoTraceWidget, StringData> {

    private List<double[]> initialDoubles;
    private List<double[]> answerDoubles;

    @Override
    public StringData getInitialAnswer() {
        return new StringData(stringFromDoubleList(initialDoubles));
    }

    @NonNull
    @Override
    public GeoTraceWidget createWidget() {
        return new GeoTraceWidget(activity, formEntryPrompt);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return stringFromDoubleList(answerDoubles);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(stringFromDoubleList(answerDoubles));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        initialDoubles = getRandomDoubleArrayList();
        answerDoubles = getRandomDoubleArrayList();
    }

    @Override
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        when(formEntryPrompt.getAnswerText()).thenReturn(stringFromDoubleList(initialDoubles));
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
    }

    private ArrayList<double[]> getRandomDoubleArrayList() {
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
    }

    private double[] getRandomDoubleArray() {
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }

    /**
     * Matches {@link org.javarosa.core.model.data.GeoTraceData#getDisplayText()}
     */
    private String stringFromDoubleList(List<double[]> doubleList) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (double[] doubles : doubleList) {
            if (!first) {
                b.append("; ");
            }
            first = false;
            b.append(stringFromDoubles(doubles));
        }
        return b.toString();
    }

    private String stringFromDoubles(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < doubles.length; i++) {
            b.append(doubles[i]);
            if (i != doubles.length - 1) {
                b.append(' ');
            }
        }

        return b.toString();
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        // The default basemap source is Google, which isn't available during
        // testing, probably because of GL version incompatibility.
        // Switching to OSMDroid ensures that the activity will launch.
        PrefUtils.getSharedPrefs().edit().putString(
            KEY_BASEMAP_SOURCE, BASEMAP_SOURCE_OSM).commit();

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, GeoPolyActivity.class, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.simple_button));
    }
}
