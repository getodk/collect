package org.odk.collect.android.regression;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.odk.collect.android.activities.SplashScreenActivity;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> rule = new ActivityTestRule<>(SplashScreenActivity.class);
}