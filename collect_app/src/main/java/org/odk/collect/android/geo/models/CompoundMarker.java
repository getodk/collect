package org.odk.collect.android.geo.models;

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
}
