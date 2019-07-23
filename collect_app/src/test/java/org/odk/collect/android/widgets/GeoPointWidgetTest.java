package org.odk.collect.android.widgets;

import android.content.Intent;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.ShadowPlayServicesUtil;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.annotation.Config;

import androidx.annotation.NonNull;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

@Config(shadows = {ShadowPlayServicesUtil.class})
public class GeoPointWidgetTest extends BinaryWidgetTest<GeoPointWidget, GeoPointData> {

    @Mock
    QuestionDef questionDef;

    private double[] initialDoubles;
    private double[] answerDoubles;

    @Override
    public GeoPointData getInitialAnswer() {
        return new GeoPointData(initialDoubles);
    }

    @NonNull
    @Override
    public GeoPointWidget createWidget() {
        return new GeoPointWidget(activity, formEntryPrompt);
    }

    @Override
    public Object createBinaryData(GeoPointData answerData) {
        return stringFromDoubles(answerDoubles);
    }

    @NonNull
    @Override
    public GeoPointData getNextAnswer() {
        return new GeoPointData(answerDoubles);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getQuestion()).thenReturn(questionDef);

        initialDoubles = getRandomDoubleArray();
        answerDoubles = getRandomDoubleArray();
    }

    @Override
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        when(formEntryPrompt.getAnswerText()).thenReturn(stringFromDoubles(initialDoubles));
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
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

        Intent intent = getIntentLaunchedByClick(R.id.get_location);
        assertComponentEquals(activity, GeoPointActivity.class, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.get_location));
    }
}
