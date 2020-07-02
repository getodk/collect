package odk.hedera.collect.widgets;

import android.content.Intent;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.hedera.collect.R;
import org.odk.hedera.collect.ShadowPlayServicesUtil;
import odk.hedera.collect.activities.GeoPointActivity;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.BaseGeoWidgetTest;
import org.robolectric.annotation.Config;

import androidx.annotation.NonNull;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

@Config(shadows = {ShadowPlayServicesUtil.class})
public class GeoPointWidgetTest extends BaseGeoWidgetTest<GeoPointWidget, GeoPointData> {

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
        return new GeoPointWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, GeoPointActivity.class, intent);
    }
}