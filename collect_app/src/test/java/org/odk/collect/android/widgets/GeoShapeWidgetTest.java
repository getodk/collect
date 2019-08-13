package org.odk.collect.android.widgets;

import android.content.Intent;
import androidx.annotation.NonNull;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.ShadowPlayServicesUtil;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

@Config(shadows = {ShadowPlayServicesUtil.class})
public class GeoShapeWidgetTest extends BinaryWidgetTest<GeoShapeWidget, StringData> {

    private ArrayList<double[]> initialDoubles;
    private ArrayList<double[]> answerDoubles;

    @Override
    public StringData getInitialAnswer() {
        return new StringData(stringFromDoubleList(initialDoubles));
    }

    @NonNull
    @Override
    public GeoShapeWidget createWidget() {
        return new GeoShapeWidget(activity, formEntryPrompt);
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
     * Matches {@link GeoPointData#getDisplayText()}
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

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, GeoPolyActivity.class, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.simple_button));
    }
}
