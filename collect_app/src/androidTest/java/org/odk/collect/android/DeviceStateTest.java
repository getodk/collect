package org.odk.collect.android;

import android.os.Build;
import android.provider.Settings;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AboutPreferencesActivity;

import static org.junit.Assert.assertFalse;

/**
 * Tests to ensure the device is setup properly to run the tests. Before making changes to a test
 * or researching a failure, make sure this set of tests pass.
 */
@RunWith(AndroidJUnit4.class)
public class DeviceStateTest {

    @Rule
    public ActivityTestRule<AboutPreferencesActivity> mActivityRule =
            new ActivityTestRule<>(
                    AboutPreferencesActivity.class);

    @Test
    public void animator_areDisabled() {
        // Espresso tests can be flaky when animations are enabled. Test devices should have them
        // disabled. This can be toggled on the Android Device by opening the Settings App:
        // Enable Developer Options, then Developer Options -> Animator duration scale = Animation off
        float animationSetting = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            animationSetting = Settings.Global.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    -2F);
        } else {
            //noinspection deprecation This statement can be removed once minSdk >= 17.
            animationSetting = Settings.System.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.System.ANIMATOR_DURATION_SCALE,
                    -2F);
        }
        // -2 indicates the value was never set, by default most devices have this enabled.
        assertFalse("Setting has never been set and the default value is invalid. " +
                "Set the value. " + animationSetting, animationSetting == -2F);
        // -1 indicates an issue looking up the value.
        assertFalse("Could not determine animations settings, " + animationSetting,
                animationSetting < 0);
        // The value should be Zero as positive values mean it is enabled.
        assertFalse("Animations are enabled; they should be disabled when running tests, " +
                animationSetting, animationSetting > 0);
    }

    @Test
    public void windowAnimations_areDisabled() {
        // Espresso tests can be flaky when animations are enabled. Test devices should have them
        // disabled. This can be toggled on the Android Device by opening the Settings App:
        // Enable Developer Options, then Developer Options -> Window animation scale = Animation off
        float winAnimationSetting = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            winAnimationSetting = Settings.Global.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.Global.WINDOW_ANIMATION_SCALE,
                    -2F);
        } else {
            //noinspection deprecation This statement can be removed once minSdk >= 17.
            winAnimationSetting = Settings.System.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.System.WINDOW_ANIMATION_SCALE,
                    -2F);
        }
        // -2 indicates the value was never set, by default most devices have this enabled.
        assertFalse("Setting has never been set and the default value is invalid. " +
                "Set the value. " + winAnimationSetting, winAnimationSetting == -2F);
        // -1 indicates an issue looking up the value.
        assertFalse("Could not determine animations settings, " + winAnimationSetting,
                winAnimationSetting < 0);
        // The value should be Zero as positive values mean it is enabled.
        assertFalse("Animations are enabled; they should be disabled when running tests, " +
                winAnimationSetting, winAnimationSetting > 0);
    }

    @Test
    public void transitions_areDisabled() {
        // Espresso tests can be flaky when animations are enabled. Test devices should have them
        // disabled. This can be toggled on the Android Device by opening the Settings App:
        // Enable Developer Options, then Developer Options -> Transition animation scale = Animation off
        float transitionSetting = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            transitionSetting = Settings.Global.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    -2F);
        } else {
            //noinspection deprecation This statement can be removed once minSdk >= 17.
            transitionSetting = Settings.System.getFloat(
                    mActivityRule.getActivity().getContentResolver(),
                    Settings.System.TRANSITION_ANIMATION_SCALE,
                    -2F);
        }
        // -2 indicates the value was never set, by default most devices have this enabled.
        assertFalse("Setting has never been set and the default value is invalid. " +
                "Set the value. " + transitionSetting, transitionSetting == -2F);
        // -1 indicates an issue looking up the value.
        assertFalse("Could not determine animations settings, " + transitionSetting,
                transitionSetting < 0);
        // The value should be Zero as positive values mean it is enabled.
        assertFalse("Animations are enabled; they should be disabled when running tests, " +
                transitionSetting, transitionSetting > 0);
    }
}