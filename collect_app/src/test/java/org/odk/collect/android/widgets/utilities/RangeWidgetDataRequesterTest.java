package org.odk.collect.android.widgets.utilities;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.RangeQuestion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.VERTICAL_APPEARANCE;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricTestRunner.class)
public class RangeWidgetDataRequesterTest {
    private final RangeQuestion rangeQuestion = mock(RangeQuestion.class);

    @Before
    public void setUp() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void getLayoutElements_forHorizontalSliderWidget_returnsCorrectSlider() {
        RangeWidgetDataRequester.RangeWidgetLayoutElements layoutElements = RangeWidgetDataRequester.getLayoutElements(
                widgetTestActivity(), null);
        assertThat(layoutElements.getSlider().getRotation(), equalTo(0.0F));
    }

    @Test
    public void getLayoutElements_forVerticalSliderWidget_returnsCorrectSlider() {
        RangeWidgetDataRequester.RangeWidgetLayoutElements layoutElements = RangeWidgetDataRequester.getLayoutElements(
                widgetTestActivity(), VERTICAL_APPEARANCE);
        assertThat(layoutElements.getSlider().getRotation(), equalTo(270.0F));
    }

    @Test
    public void whenRangeQuestionHasZeroRangeStep_invalidWidgetToastIsShown() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        assertThat(RangeWidgetDataRequester.isWidgetValid(rangeQuestion), equalTo(false));
        assertThat(ShadowToast.getTextOfLatestToast(),
                equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShown() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(2));
        assertThat(RangeWidgetDataRequester.isWidgetValid(rangeQuestion), equalTo(false));
        assertThat(ShadowToast.getTextOfLatestToast(),
                equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }
}
