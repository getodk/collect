package org.odk.collect.android.widgets;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Button;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.junit.Before;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.ShadowPlayServicesUtil;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.GeoPointWidget.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;

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

    // todo: add more tests for different appearances
    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;

        switch (clickedButton.getId()) {
            case R.id.get_point:
            case R.id.get_location:
                if (permissionGranted) {
                    intent = new Intent(activity, GeoPointMapActivity.class);
                    intent.putExtra(READ_ONLY, false);
                    intent.putExtra(DRAGGABLE_ONLY, true);
                    intent.putExtra(ACCURACY_THRESHOLD, 5.0);
                }
                break;
        }
        return intent;
    }
}
