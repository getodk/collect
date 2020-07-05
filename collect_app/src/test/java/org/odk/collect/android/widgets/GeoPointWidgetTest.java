package org.odk.collect.android.widgets;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {

    private List<double[]> answerDoubles;
    private QuestionDef questionDef;
    private MapConfigurator mapConfigurator;

    @Before
    public void setup() {
        answerDoubles = getRandomDoubleArrayList();
        questionDef = mock(QuestionDef.class);
        mapConfigurator = mock(MapConfigurator.class);
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), equalTo(null));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveConvertibleString_returnsNull() throws NumberFormatException {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer(), equalTo(null));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(stringFromDoubleList(answerDoubles))));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(stringFromDoubleList(answerDoubles)));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(stringFromDoubleList(answerDoubles))));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void usingReadOnlyOption_makesAllClickableElementsDisabled() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.findViewById(R.id.simple_button).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        TextView textView = widget.findViewById(R.id.geo_answer_text);
        assertThat(textView.getText().toString(),equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        String answer = stringFromDoubleList(answerDoubles);
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));

        TextView textView = widget.findViewById(R.id.geo_answer_text);
        String[] parts = answer.split(" ");

        assertThat(textView.getText().toString(),equalTo(widget.getContext().getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        )));
    }

    @Test
    public void settingAnswer_callsValueChangeListener() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        TextView textView = widget.findViewById(R.id.geo_answer_text);
        textView.setText(stringFromDoubleList(answerDoubles));

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void whenWidgetHasAppearanceAndNullAsAnswer_buttonShowsCorrectText() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearanceAndAnswer(PLACEMENT_MAP, null));
        assertThat(((Button) widget.findViewById(R.id.simple_button)).getText(),
                equalTo(widget.getContext().getString(R.string.get_point)));
    }

    @Test
    public void whenWidgetHasAppearanceAndAnswer_buttonShowsCorrectText() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearanceAndAnswer(PLACEMENT_MAP, new StringData(stringFromDoubleList(answerDoubles))));
        assertThat(((Button) widget.findViewById(R.id.simple_button)).getText(),
                equalTo(widget.getContext().getString(R.string.view_change_location)));
    }

    @Test
    public void whenWidgetHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(stringFromDoubleList(answerDoubles))));

        assertThat(((Button) widget.findViewById(R.id.simple_button)).getText(),
                equalTo(widget.getContext().getString(R.string.change_location)));
    }

    @Test
    public void whenWidgetHasNullAsAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));

        assertThat(((Button) widget.findViewById(R.id.simple_button)).getText(),
                equalTo(widget.getContext().getString(R.string.get_point)));
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), questionDef, mapConfigurator);
    }

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

    private ArrayList<double[]> getRandomDoubleArrayList() {
        Random random = new Random();
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
    }

    private double[] getRandomDoubleArray() {
        Random random = new Random();
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }
}