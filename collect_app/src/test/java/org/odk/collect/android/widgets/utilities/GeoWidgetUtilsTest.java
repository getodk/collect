package org.odk.collect.android.widgets.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.StringData;
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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoWidgetUtilsTest {
    private static final String STRING_ARG = "STRING_ARG";

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final GeoWidgetUtils geoWidgetUtils = new GeoWidgetUtils(permissionUtils);
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());
    private final Bundle bundle = new Bundle();

    private TestScreenContextActivity testActivity;
    private Context context;
    private FormEntryPrompt prompt;
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
    public void requestGeoPoint_returnsCorrectData() {
        prompt = promptWithReadOnly();
        when(prompt.getQuestion()).thenReturn(questionDef);

        assertGeoPointBundleArgumentEquals(geoWidgetUtils.requestGeoPoint(prompt), null, DEFAULT_LOCATION_ACCURACY, true, false);

        GeoPointData answer = new GeoPointData(getRandomDoubleArray());
        prompt = promptWithAppearanceAndAnswer(WidgetAppearanceUtils.MAPS, answer);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("10");

        assertGeoPointBundleArgumentEquals(geoWidgetUtils.requestGeoPoint(prompt), GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                10.0, false, false);

        prompt = promptWithAppearanceAndAnswer(WidgetAppearanceUtils.PLACEMENT_MAP, answer);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("7");

        assertGeoPointBundleArgumentEquals(geoWidgetUtils.requestGeoPoint(prompt), GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                7.0, false, true);
    }

    @Test
    public void requestGeoShape_returnsCorrectData() {
        prompt = promptWithReadOnly();
        when(prompt.getQuestion()).thenReturn(questionDef);
        assertGeoPolyBundleArgumentEquals(geoWidgetUtils.requestGeoShape(prompt), null, GeoPolyActivity.OutputMode.GEOSHAPE, true);

        prompt = promptWithAnswer(new StringData("blah"));
        when(prompt.getQuestion()).thenReturn(questionDef);
        assertGeoPolyBundleArgumentEquals(geoWidgetUtils.requestGeoShape(prompt), "blah", GeoPolyActivity.OutputMode.GEOSHAPE, false);
    }

    @Test
    public void requestGeoTrace_returnsCorrectData() {
        prompt = promptWithReadOnly();
        when(prompt.getQuestion()).thenReturn(questionDef);
        assertGeoPolyBundleArgumentEquals(geoWidgetUtils.requestGeoTrace(prompt), null, GeoPolyActivity.OutputMode.GEOTRACE, true);

        prompt = promptWithAnswer(new StringData("blah"));
        when(prompt.getQuestion()).thenReturn(questionDef);
        assertGeoPolyBundleArgumentEquals(geoWidgetUtils.requestGeoTrace(prompt), "blah", GeoPolyActivity.OutputMode.GEOTRACE, false);
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(GeoWidgetUtils.getAnswerToDisplay(context, null), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(GeoWidgetUtils.getAnswerToDisplay(context, "blah"), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        String stringAnswer = answer.getDisplayText();
        String[] parts = stringAnswer.split(" ");
        assertEquals(GeoWidgetUtils.getAnswerToDisplay(context, stringAnswer), context.getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        ));
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoIntentShouldNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(testActivity).getNextStartedActivity();

        assertNull(startedIntent);
        assertFalse(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIsGranted_requestGeoIntentSetsWidgetWaitingForData() {
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoPointWidget() {
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoPointMapWidget() {
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPointMapActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoShapeWidget() {
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPolyActivity.class, bundle, GEOSHAPE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void requestGeoIntentShouldLaunchCorrectIntent_forGeoTraceWidget() {
        geoWidgetUtils.requestGeoIntent(testActivity, formIndex, waitingForDataRegistry,
                GeoPolyActivity.class, bundle, GEOTRACE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    // Results confirmed with https://www.sunearthtools.com/dp/tools/conversion.php
    public void convertCoordinatesIntoDegreeFormatTest() {
        assertEquals("N 37°27'5\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 37.45153333333334, "lat"));
        assertEquals("W 122°9'19\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, -122.15539166666667, "lon"));

        assertEquals("N 3°51'4\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 3.8513583333333337, "lat"));
        assertEquals("W 70°2'11\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, -70.03650333333333, "lon"));

        assertEquals("S 31°8'40\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, -31.144546666666663, "lat"));
        assertEquals("E 138°16'15\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 138.27083666666667, "lon"));

        assertEquals("N 61°23'15\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 61.38757333333333, "lat"));
        assertEquals("W 150°55'37\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, -150.92708666666667, "lon"));

        assertEquals("N 0°0'0\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 0, "lat"));
        assertEquals("E 0°0'0\"", GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, 0, "lon"));
    }

    @Test
    public void floorTest() {
        assertEquals("5", GeoWidgetUtils.floor("5"));
        assertEquals("-5", GeoWidgetUtils.floor("-5"));
        assertEquals("5", GeoWidgetUtils.floor("5.55"));
        assertEquals("-5", GeoWidgetUtils.floor("-5.55"));
        assertEquals("", GeoWidgetUtils.floor(""));
        assertEquals("", GeoWidgetUtils.floor(null));
        assertEquals("qwerty", GeoWidgetUtils.floor("qwerty"));
    }

    @Test
    public void getLocationParamsFromStringAnswerTest() {
        double[] gp = GeoWidgetUtils.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 20.0");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(20.0, gp[3]);

        gp = GeoWidgetUtils.getLocationParamsFromStringAnswer("37.45153333333334");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoWidgetUtils.getLocationParamsFromStringAnswer("");
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoWidgetUtils.getLocationParamsFromStringAnswer(null);
        assertEquals(0.0, gp[0]);
        assertEquals(0.0, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);

        gp = GeoWidgetUtils.getLocationParamsFromStringAnswer("37.45153333333334 -122.15539166666667 0.0 qwerty");
        assertEquals(37.45153333333334, gp[0]);
        assertEquals(-122.15539166666667, gp[1]);
        assertEquals(0.0, gp[2]);
        assertEquals(0.0, gp[3]);
    }

    @Test
    public void truncateDoubleTest() {
        assertEquals("5", GeoWidgetUtils.truncateDouble("5"));
        assertEquals("-5", GeoWidgetUtils.truncateDouble("-5"));
        assertEquals("5.12", GeoWidgetUtils.truncateDouble("5.12"));
        assertEquals("-5.12", GeoWidgetUtils.truncateDouble("-5.12"));
        assertEquals("5.12", GeoWidgetUtils.truncateDouble("5.1234"));
        assertEquals("-5.12", GeoWidgetUtils.truncateDouble("-5.1234"));
        assertEquals("", GeoWidgetUtils.truncateDouble(""));
        assertEquals("", GeoWidgetUtils.truncateDouble(null));
        assertEquals("", GeoWidgetUtils.truncateDouble("qwerty"));
    }
}
