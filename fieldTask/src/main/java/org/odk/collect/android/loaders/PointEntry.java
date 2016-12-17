package org.odk.collect.android.loaders;

/**
 * This class holds the per-item data in the {@link MapDataLoader}.
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