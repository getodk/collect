package org.odk.collect.android.widgets.utilities;

import static junit.framework.TestCase.assertEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.truncateDouble;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.data.GeoPointData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.geo.GeoUtils;

@RunWith(AndroidJUnit4.class)
public class GeoWidgetUtilsTest {

    private final Context context = ApplicationProvider.getApplicationContext();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    @Test
    public void getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(GeoWidgetUtils.getGeoPointAnswerToDisplay(context, null), "");
    }

    @Test
    public void getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(GeoWidgetUtils.getGeoPointAnswerToDisplay(context, "blah"), "");
    }

    @Test
    //Cm accuracy #4198
    public void getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        String stringAnswer = answer.getDisplayText();
        String actual = GeoWidgetUtils.getGeoPointAnswerToDisplay(context, stringAnswer);
        String[] parts = stringAnswer.split(" ");
        double accuracyCm = Double.parseDouble(parts[3]) * 100;
        String expected = context.getString(
                R.string.gps_result_cm,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                truncateDouble(parts[2]),
                accuracyCm
        );
        assertEquals(expected, actual);
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
        assertEquals("5", truncateDouble("5"));
        assertEquals("-5", truncateDouble("-5"));
        assertEquals("5.12", truncateDouble("5.12"));
        assertEquals("-5.12", truncateDouble("-5.12"));
        assertEquals("5.12", truncateDouble("5.1234"));
        assertEquals("-5.12", truncateDouble("-5.1234"));
        assertEquals("", truncateDouble(""));
        assertEquals("", truncateDouble(null));
        assertEquals("", truncateDouble("qwerty"));
    }

    @Test
    //Cm accuracy #4198
    public void locationAccuracyIsFormattedInAppropriateUnit() {
        for (double accuracy : GeoUtils.TEST_ACCURACIES) {
            boolean useCm = accuracy < 1;
            String answer = "1 1 1 " + accuracy;
            String[] parts = answer.split(" ");
            String expectedAccuracy = context.getString(useCm
                            ? R.string.gps_result_cm : R.string.gps_result,
                    parts[0], parts[1], parts[2],
                    useCm ? accuracy * 100 : truncateDouble(parts[3]))
                    .split("\n")[3];
            String actualAccuracy = GeoWidgetUtils.getGeoPointAnswerToDisplay(context, (String) answer)
                    .split("\n")[3];
            assertEquals(expectedAccuracy, actualAccuracy);
        }
    }
}
