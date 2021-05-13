package org.odk.collect.android.widgets.utilities;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.data.GeoPointData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static junit.framework.TestCase.assertEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;

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
    public void getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        String stringAnswer = answer.getDisplayText();
        String[] parts = stringAnswer.split(" ");
        assertEquals(GeoWidgetUtils.getGeoPointAnswerToDisplay(context, stringAnswer), context.getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        ));
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
