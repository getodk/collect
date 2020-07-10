package org.odk.collect.android.views;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class CustomRangeSliderTest {

    private CustomRangeSlider slider;

    @Before
    public void setUp() {
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);
        slider = new CustomRangeSlider(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void onStartTrackingTouch_suppressesFlingGesture() {
        slider.onStartTrackingTouch(slider);
        assertThat(slider.suppressFlingGesture, equalTo(true));
    }

    @Test
    public void onStopTrackingTouch_doesNotSuppressFlingGesture() {
        slider.onStopTrackingTouch(slider);
        assertThat(slider.suppressFlingGesture, equalTo(false));
    }
}
