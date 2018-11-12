package org.odk.collect.android.widgets.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.mocks.MockedPermissionUtils;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;


/**
 * @author Shobhit Agarwal
 */
public abstract class ButtonWidgetTest<W extends ButtonWidget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    private final MockedPermissionUtils permissionUtils;

    ButtonWidgetTest() {
        permissionUtils = new MockedPermissionUtils(Shadow.newInstanceOf(Activity.class));
    }

    private void stubAllRuntimePermissionsGranted(boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        ((QuestionWidget) getActualWidget()).setPermissionUtils(permissionUtils);
    }

    @Test
    public void performButtonClickShouldLaunchIntentWhenPermissionGranted() {
        stubAllRuntimePermissionsGranted(true);

        runButtonClickTest(true);
    }

    @Test
    public void performButtonClickShouldNotLaunchIntentWhenPermissionNotGranted() {
        stubAllRuntimePermissionsGranted(false);

        runButtonClickTest(false);
    }

    private void runButtonClickTest(boolean isPermissionGranted) {
        for (Button button : getWidget().getSimpleButtonList()) {

            // perform button click
            button.performClick();

            Intent expectedIntent = getExpectedIntent(button, isPermissionGranted);

            // obtain launched intent
            ShadowActivity shadowActivity = shadowOf(activity);
            Intent startedIntent = shadowActivity.getNextStartedActivity();

            // assert results
            assertIntentEquals(expectedIntent, startedIntent);
        }
    }

    /**
     * Get the expected {@link Intent} from the widget which will be launched
     *
     *  @param clickedButton     The button which was clicked
     * @param permissionGranted Whether permission was granted while triggering the button
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        return null;
    }

    /**
     * Things to be asserted:
     * <p>
     * 1. If a permission is required before triggering activity, then the intent should only be
     * launched if the permissions are granted, otherwise not.
     * <p>
     * 2. Assert that the triggered intent has expected COMPONENT, ACTION, EXTRAS and TYPE
     */
    private void assertIntentEquals(Intent expectedIntent, Intent launchedIntent) {

        if (expectedIntent == null && launchedIntent == null) {
            return;
        }

        assertNotNull(expectedIntent);
        assertNotNull(launchedIntent);

        assertEquals(expectedIntent.getPackage(), launchedIntent.getPackage());
        assertEquals(expectedIntent.getClass(), launchedIntent.getClass());
        assertEquals(expectedIntent.getAction(), launchedIntent.getAction());
        assertEquals(expectedIntent.getType(), launchedIntent.getType());
        assertBundleEquals(expectedIntent.getExtras(), launchedIntent.getExtras());
    }

    /**
     * Asserts that both bundles are similar
     */
    private void assertBundleEquals(Bundle expectedBundle, Bundle actualBundle) {
        // check if both are null
        if (expectedBundle == null && actualBundle == null) {
            return;
        }

        // assert if both are not null and have same number of extras
        assertNotNull(expectedBundle);
        assertNotNull(actualBundle);
        assertEquals(expectedBundle.size(), actualBundle.size());

        // assert that (key, value) pair is in both bundles
        for (String expectedKey : expectedBundle.keySet()) {
            Object expectedValue = expectedBundle.get(expectedKey);
            Object actualValue = actualBundle.get(expectedKey);

            /*
             * Iterate through each value if value is an int array
             *
             * Required because of {@link AlignedImageWidget#DIMENSIONS_EXTRA}
             */
            if (expectedValue instanceof int[]) {
                for (int i = 0; i < ((int[]) expectedValue).length; i++) {
                    assertEquals(((int[]) expectedValue)[i], ((int[]) actualValue)[i]);
                }
            } else {
                assertEquals("Value mismatch for extra key: " + expectedKey + "\n", expectedValue, actualValue);
            }
        }
    }
}
