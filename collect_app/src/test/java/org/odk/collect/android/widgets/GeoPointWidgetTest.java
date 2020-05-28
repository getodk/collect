package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {

    @Test
    public void usingReadOnlyOption_makesAllClickableElementsDisabled() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.findViewById(R.id.simple_button).getVisibility(), equalTo(View.GONE));
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}



/*
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

    */
/**
     * Matches {@link GeoPointData#getDisplayText()}
     *//*

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
}*/
