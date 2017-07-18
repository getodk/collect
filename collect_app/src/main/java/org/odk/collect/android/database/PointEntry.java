package org.odk.collect.android.database;

/**
 * This class holds the per-item data.
 */
public class PointEntry {
    public double lat;    // form or task
    public double lon;
    public long time;
    @Override
    public String toString() {
        return "Point(" + lat + "," + lon + ")";
    }

}