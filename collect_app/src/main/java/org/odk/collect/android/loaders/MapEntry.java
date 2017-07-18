package org.odk.collect.android.loaders;

import java.util.ArrayList;

/**
 * This class holds the per-item data in the {@link org.odk.collect.android.loaders.MapDataLoader}.
 */
public class MapEntry {
    public ArrayList<TaskEntry> tasks;    // form or task
    public ArrayList<PointEntry> points;    // form or task
    @Override
    public String toString() {
        return tasks.size() + " points and " + points.size();
    }

}