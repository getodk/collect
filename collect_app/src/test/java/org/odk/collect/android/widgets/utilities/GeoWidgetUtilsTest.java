package org.odk.collect.android.widgets.utilities;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class GeoWidgetUtilsTest {

    @Test
    public void convertCoordinatesIntoDegreeFormatTest() {

    }

    @Test
    public void floorTest() {
        assertEquals("5", GeoWidgetUtils.floor("5"));
        assertEquals("-5", GeoWidgetUtils.floor("-5"));
        assertEquals("5", GeoWidgetUtils.floor("5.55"));
        assertEquals("-5", GeoWidgetUtils.floor("-5.55"));
    }

    @Test
    public void getLocationParamsFromStringAnswerTest() {

    }

    @Test
    public void truncateDoubleTest() {
        assertEquals("5", GeoWidgetUtils.truncateDouble("5"));
        assertEquals("-5", GeoWidgetUtils.truncateDouble("-5"));
        assertEquals("5.12", GeoWidgetUtils.truncateDouble("5.12"));
        assertEquals("-5.12", GeoWidgetUtils.truncateDouble("-5.12"));
        assertEquals("5.12", GeoWidgetUtils.truncateDouble("5.1234"));
        assertEquals("-5.12", GeoWidgetUtils.truncateDouble("-5.1234"));
    }
}
