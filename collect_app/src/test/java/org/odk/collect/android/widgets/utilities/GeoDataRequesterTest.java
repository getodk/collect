package org.odk.collect.android.widgets.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());
    private final Bundle bundle = new Bundle();

    private GeoWidgetAnswerBinding binding;
    private WaitingForDataRegistry waitingForDataRegistry;
    private TestScreenContextActivity testActivity;
    private Context context;
    private FormIndex formIndex;
    private MapConfigurator mapConfigurator;
    private QuestionDef questionDef;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testActivity = widgetTestActivity();
        binding = GeoWidgetAnswerBinding.inflate(testActivity.getLayoutInflater());

        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        formIndex = mock(FormIndex.class);
        mapConfigurator = mock(MapConfigurator.class);
        questionDef = mock(QuestionDef.class);

        when(mapConfigurator.isAvailable(any())).thenReturn(true);
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
    public void whenPermissionIsNotGranted_onButtonClickedShouldNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, null,
                waitingForDataRegistry, GeoPointActivity.class, null, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(testActivity).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenMapConfiguratorIsNotAvailable_widgetShowsUnavailableMessage() {
        when(mapConfigurator.isAvailable(any())).thenReturn(false);
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, mapConfigurator,
                waitingForDataRegistry, GeoPointActivity.class, null, LOCATION_CAPTURE);

        verify(mapConfigurator).showUnavailableMessage(testActivity);
    }

    @Test
    public void whenPermissionIsGranted_onButtonClickedWaitsForLocationData() {
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, mapConfigurator,
                waitingForDataRegistry, GeoPointActivity.class, bundle, LOCATION_CAPTURE);

        verify(waitingForDataRegistry).waitForData(formIndex);
    }

    @Test
    public void onButtonClickedShouldLaunchCorrectIntent_forGeoPointWidget() {
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, null,
                waitingForDataRegistry, GeoPointActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void onButtonClickedShouldLaunchCorrectIntent_forGeoPointMapWidget() {
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, null,
                waitingForDataRegistry, GeoPointMapActivity.class, bundle, LOCATION_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void onButtonClickedShouldLaunchCorrectIntent_forGeoShapeWidget() {
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, null,
                waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOSHAPE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void onButtonClickedShouldLaunchCorrectIntent_forGeoTraceWidget() {
        geoDataRequester.onButtonClicked(testActivity, formIndex, permissionUtils, null,
                waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOTRACE_CAPTURE);
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        assertEquals(startedIntent.getExtras().getString(STRING_ARG), "blah");
    }

    @Test
    public void setButtonLabelAndVisibility_doesNotShowButton_whenWidgetIsReadOnlyAndDoesNotHaveAnswer() {
        geoDataRequester.setButtonLabelAndVisibility(binding, true, false,
                R.string.geopoint_view_read_only, R.string.view_change_location, R.string.get_point);
        assertEquals(binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void setButtonLabelAndVisibility_showsButtonWithCorrectLabel_whenWidgetIsReadOnlyAndHasAnswerData() {
        geoDataRequester.setButtonLabelAndVisibility(binding, true, true,
                R.string.geopoint_view_read_only, R.string.view_change_location, R.string.get_point);
        assertEquals(binding.simpleButton.getText(), context.getString(R.string.geopoint_view_read_only));
    }

    @Test
    public void setButtonLabelAndVisibility_showsButtonWithCorrectLabel_whenWidgetIsNotReadOnlyAndDoesNotHaveAnswer() {
        geoDataRequester.setButtonLabelAndVisibility(binding, false, false,
                R.string.geopoint_view_read_only, R.string.view_change_location, R.string.get_point);
        assertEquals(binding.simpleButton.getText(), context.getString(R.string.get_point));
    }

    @Test
    public void setButtonLabelAndVisibility_showsButtonWithCorrectLabel_whenWidgetIsNotReadOnlyAndHasAnswer() {
        geoDataRequester.setButtonLabelAndVisibility(binding, false, true,
                R.string.geopoint_view_read_only, R.string.view_change_location, R.string.get_point);
        assertEquals(binding.simpleButton.getText(), context.getString(R.string.view_change_location));
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
