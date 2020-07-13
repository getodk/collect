package org.odk.collect.android.widgets.utilities;

import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.RangeQuestion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.views.SuppressFlingGestureSlider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndRangeQuestion;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangeWidgetUtilsTest {

    private RangeQuestion rangeQuestion;
    private SuppressFlingGestureSlider slider;
    private TextView sampleTextView1;
    private TextView sampleTextView2;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);

        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);
        slider = new SuppressFlingGestureSlider(ApplicationProvider.getApplicationContext());
        sampleTextView1 = new TextView(ApplicationProvider.getApplicationContext());
        sampleTextView2 = new TextView(ApplicationProvider.getApplicationContext());

        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void usingReadOnlyOption_disablesTheSlider() {
        Object[] objects = RangeWidgetUtils.setUpLayoutElements(widgetTestActivity(), promptWithReadOnlyAndRangeQuestion(rangeQuestion));
        assertThat(((SuppressFlingGestureSlider) objects[1]).isEnabled(), equalTo(false));
    }

    @Test
    public void setUpWidgetParameters_showsCorrectMinAndMaxValues() {
        RangeWidgetUtils.setUpWidgetParameters(rangeQuestion, sampleTextView1, sampleTextView2);

        assertThat(sampleTextView1.getText(), equalTo("1"));
        assertThat(sampleTextView2.getText(), equalTo("10"));
    }

    @Test
    public void setUpSlider_shouldShowCorrectSlider() {
        RangeWidgetUtils.setUpSlider(rangeQuestion, slider, new BigDecimal("5"));

        assertThat(slider.getValueFrom(), equalTo(1.0F));
        assertThat(slider.getValueTo(), equalTo(10.0F));
        assertThat(slider.getStepSize(), equalTo(1.0F));
        assertThat(slider.getValue(), equalTo(5.0F));
    }

    @Test
    public void setUpNullValue_returnsNullValueAndSetsCorrectValuesInSliderAndAnswerTextView() {
        BigDecimal value = RangeWidgetUtils.setUpNullValue(slider, sampleTextView1);

        assertThat(value, equalTo(null));
        assertThat(slider.getValue(), equalTo(slider.getValueFrom()));
        assertThat(sampleTextView1.getText(), equalTo(""));
    }

    @Test
    public void whenRangeQuestionHasZeroRangeStep_invalidWidgetToastIsShownAndSliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        RangeWidgetUtils.isWidgetValid(rangeQuestion, slider);
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(slider.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShownAndSliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(2));
        RangeWidgetUtils.isWidgetValid(rangeQuestion, slider);
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(slider.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }
}
