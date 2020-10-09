package org.odk.collect.android.widgets.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.android.widgets.utilities.GeoDataRequester.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.utilities.GeoDataRequester.DEFAULT_LOCATION_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoDataRequesterTest {
    private static final String STRING_ARG = "STRING_ARG";

    private final GeoDataRequester geoDataRequester = new GeoDataRequester();
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());
    private final Bundle bundle = new Bundle();

    private TestScreenContextActivity testActivity;
    private Context context;
    private FormIndex formIndex;
    private QuestionDef questionDef;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testActivity = widgetTestActivity();

        formIndex = mock(FormIndex.class);
        questionDef = mock(QuestionDef.class);

        permissionUtils.setPermissionGranted(true);
        bundle.putString(STRING_ARG, "blah");
    }

    @Test
    public void getAccuracyThreshold_whenAccuracyThresholdIsNull_returnsDefaultValue() {
        assertEquals(GeoDataRequester.getAccuracyThreshold(questionDef), DEFAULT_LOCATION_ACCURACY);
    }

    @Test
    public void getAccuracyThreshold_whenAccuracyThresholdIsNotNull_returnsAccuracyThreshold() {
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("3");
        assertEquals(GeoDataRequester.getAccuracyThreshold(questionDef), Double.parseDouble("3"));
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(GeoDataRequester.getAnswerToDisplay(context, null), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(GeoDataRequester.getAnswerToDisplay(context, "blah"), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        String stringAnswer = answer.getDisplayText();
        String[] parts = stringAnswer.split(" ");
        assertEquals(GeoDataRequester.getAnswerToDisplay(context, stringAnswer), context.getString(
                R.string.gps_result,
                GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                GeoDataRequester.truncateDouble(parts[2]),
                GeoDataRequester.truncateDouble(parts[3])
        ));
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoIntentShouldNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPointActivity.class, null, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(testActivity).getNextStartedActivity();

        assertNull(startedIntent);
        assertFalse(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIsGranted_requestGeoIntentSetsWidgetWaitingForData() {
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoPointWidget() {
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoPointMapWidget() {
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPointMapActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntenrShouldLaunchCorrectIntent_forGeoShapeWidget() {
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOSHAPE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoTraceWidget() {
        geoDataRequester.requestGeoIntent(testActivity, formIndex, permissionUtils,
                waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOTRACE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    // Results confirmed with https://www.sunearthtools.com/dp/tools/conversion.php
    public void convertCoordinatesIntoDegreeFormatTest() {
        assertEquals("N 37°27'5\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 37.45153333333334, "lat"));
        assertEquals("W 122°9'19\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -122.15539166666667, "lon"));

        assertEquals("N 3°51'4\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 3.8513583333333337, "lat"));
        assertEquals("W 70°2'11\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -70.03650333333333, "lon"));

        assertEquals("S 31°8'40\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -31.144546666666663, "lat"));
        assertEquals("E 138°16'15\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 138.27083666666667, "lon"));

        assertEquals("N 61°23'15\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 61.38757333333333, "lat"));
        assertEquals("W 150°55'37\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, -150.92708666666667, "lon"));

        assertEquals("N 0°0'0\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 0, "lat"));
        assertEquals("E 0°0'0\"", GeoDataRequester.convertCoordinatesIntoDegreeFormat(context, 0, "lon"));
    }

    @Test
    public void floorTest() {
        assertEquals("5", GeoDataRequester.floor("5"));
        assertEquals("-5", GeoDataRequester.floor("-5"));
        assertEquals("5", GeoDataRequester.floor("5.55"));
        assertEquals("-5", GeoDataRequester.floor("-5.55"));
        assertEquals("", GeoDataRequester.floor(""));
        assertEquals("", GeoDataRequester.floor(null));
        assertEquals("qwerty", GeoDataRequester.floor("qwerty"));
    }

    @Test
    public void getLocationParamsFromStringAnswerTest() {
        double[] gp = GeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 20.0");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(20.0, gp[3]);

        gp = GeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoDataRequester.getLocationParamsFromStringAnswer("");
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoDataRequester.getLocationParamsFromStringAnswer(null);
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoDataRequester.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 qwerty");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);
    }

    @Test
    public void truncateDoubleTest() {
        assertEquals("5", GeoDataRequester.truncateDouble("5"));
        assertEquals("-5", GeoDataRequester.truncateDouble("-5"));
        assertEquals("5.12", GeoDataRequester.truncateDouble("5.12"));
        assertEquals("-5.12", GeoDataRequester.truncateDouble("-5.12"));
        assertEquals("5.12", GeoDataRequester.truncateDouble("5.1234"));
        assertEquals("-5.12", GeoDataRequester.truncateDouble("-5.1234"));
        assertEquals("", GeoDataRequester.truncateDouble(""));
        assertEquals("", GeoDataRequester.truncateDouble(null));
        assertEquals("", GeoDataRequester.truncateDouble("qwerty"));
    }
}
