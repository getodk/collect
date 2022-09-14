package org.odk.collect.android.geo.models;

import org.odk.collect.android.R;

public class CompoundMarker {
    public static final String MARKER_NONE = "none";
    public static final String MARKER_PIT = "pit";
    public static final String MARKER_FAULT = "fault";

    public int index;
    public String type;

    public CompoundMarker() {

    }

    public CompoundMarker(int index, String type) {
        this.index = index;
        this.type = type;
    }

    public int getDrawableIdForMarker() {
        switch (type) {
            case CompoundMarker.MARKER_PIT:
                return R.drawable.ic_map_point_pit;
            case CompoundMarker.MARKER_FAULT:
                return R.drawable.ic_map_point_fault;
        }
        return R.drawable.ic_map_point;
    }
}
