package org.odk.collect.android.widgets.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.DEFAULT_LOCATION_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ActivityGeoDataRequesterTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final ActivityGeoDataRequester activityGeoDataRequester = new ActivityGeoDataRequester(permissionUtils);
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private TestScreenContextActivity testActivity;
    private ShadowActivity shadowActivity;
    private Context context;
    private FormEntryPrompt prompt;
    private FormIndex formIndex;
    private QuestionDef questionDef;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testActivity = widgetTestActivity();
        shadowActivity = shadowOf(testActivity);

        prompt = promptWithReadOnly();
        formIndex = mock(FormIndex.class);
        questionDef = mock(QuestionDef.class);

        permissionUtils.setPermissionGranted(true);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getIndex()).thenReturn(formIndex);
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoPoint_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoShape_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoTrace_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIGranted_requestGeoPoint_launchCorrectIntent_andSetsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, DEFAULT_LOCATION_ACCURACY, true, false);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));

        when(prompt.isReadOnly()).thenReturn(false);
        when(prompt.getAnswerText()).thenReturn(answer.getDisplayText());
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("10");

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), ActivityGeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                10.0, false, false);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));

        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.PLACEMENT_MAP);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), ActivityGeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                10.0, false, true);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));

        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.MAPS);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), ActivityGeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                10.0, false, false);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoShape_launchCorrectIntent_andSetsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), null, GeoPolyActivity.OutputMode.GEOSHAPE, true);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));

        when(prompt.isReadOnly()).thenReturn(false);
        when(prompt.getAnswerText()).thenReturn("blah");

        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);
        startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOSHAPE, false);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoTrace_launchCorrectIntent_andSetsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), null, GeoPolyActivity.OutputMode.GEOTRACE, true);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));

        when(prompt.isReadOnly()).thenReturn(false);
        when(prompt.getAnswerText()).thenReturn("blah");

        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);
        startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOTRACE, false);
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(ActivityGeoDataRequester.getAnswerToDisplay(context, null), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(ActivityGeoDataRequester.getAnswerToDisplay(context, "blah"), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        String stringAnswer = answer.getDisplayText();
        String[] parts = stringAnswer.split(" ");
        assertEquals(ActivityGeoDataRequester.getAnswerToDisplay(context, stringAnswer), context.getString(
                R.string.gps_result,
                ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                ActivityGeoDataRequester.truncateDouble(parts[2]),
                ActivityGeoDataRequester.truncateDouble(parts[3])
        ));
    }

    @Test
    // Results confirmed with https://www.sunearthtools.com/dp/tools/conversion.php
    public void convertCoordinatesIntoDegreeFormatTest() {
        assertEquals("N 37°27'5\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 37.45153333333334, "lat"));
        assertEquals("W 122°9'19\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -122.15539166666667, "lon"));

        assertEquals("N 3°51'4\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 3.8513583333333337, "lat"));
        assertEquals("W 70°2'11\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -70.03650333333333, "lon"));

        assertEquals("S 31°8'40\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -31.144546666666663, "lat"));
        assertEquals("E 138°16'15\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 138.27083666666667, "lon"));

        assertEquals("N 61°23'15\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 61.38757333333333, "lat"));
        assertEquals("W 150°55'37\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -150.92708666666667, "lon"));

        assertEquals("N 0°0'0\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 0, "lat"));
        assertEquals("E 0°0'0\"", ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 0, "lon"));
    }

    @Test
    public void floorTest() {
        assertEquals("5", ActivityGeoDataRequester.floor("5"));
        assertEquals("-5", ActivityGeoDataRequester.floor("-5"));
        assertEquals("5", ActivityGeoDataRequester.floor("5.55"));
        assertEquals("-5", ActivityGeoDataRequester.floor("-5.55"));
        assertEquals("", ActivityGeoDataRequester.floor(""));
        assertEquals("", ActivityGeoDataRequester.floor(null));
        assertEquals("qwerty", ActivityGeoDataRequester.floor("qwerty"));
    }

    @Test
    public void getLocationParamsFromStringAnswerTest() {
        double[] gp = ActivityGeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 20.0");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(20.0, gp[3]);

        gp = ActivityGeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = ActivityGeoDataRequester.getLocationParamsFromStringAnswer("");
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = ActivityGeoDataRequester.getLocationParamsFromStringAnswer(null);
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = ActivityGeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 qwerty");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);
    }

    @Test
    public void truncateDoubleTest() {
        assertEquals("5", ActivityGeoDataRequester.truncateDouble("5"));
        assertEquals("-5", ActivityGeoDataRequester.truncateDouble("-5"));
        assertEquals("5.12", ActivityGeoDataRequester.truncateDouble("5.12"));
        assertEquals("-5.12", ActivityGeoDataRequester.truncateDouble("-5.12"));
        assertEquals("5.12", ActivityGeoDataRequester.truncateDouble("5.1234"));
        assertEquals("-5.12", ActivityGeoDataRequester.truncateDouble("-5.1234"));
        assertEquals("", ActivityGeoDataRequester.truncateDouble(""));
        assertEquals("", ActivityGeoDataRequester.truncateDouble(null));
        assertEquals("", ActivityGeoDataRequester.truncateDouble("qwerty"));
    }
}
