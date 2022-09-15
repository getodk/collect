package org.odk.collect.android.geo.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.odk.collect.android.R;

public class CompoundMarker implements Parcelable {
    public static final String MARKER_NONE = "none";
    public static final String MARKER_PIT = "pit";
    public static final String MARKER_FAULT = "fault";

    public int index;
    public String type;
    public String label;

    public CompoundMarker() {

    }

    public CompoundMarker(int index, String type, String label) {
        this.index = index;
        this.type = type;
        this.label = label;
    }

    private CompoundMarker(Parcel parcel) {
        this.index = parcel.readInt();
        this.type = parcel.readString();
        this.label = parcel.readString();
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

    // Implementation of the Parcelable interface.

    public static final Parcelable.Creator<CompoundMarker> CREATOR = new Parcelable.Creator<CompoundMarker>() {
        public CompoundMarker createFromParcel(Parcel parcel) {
            return new CompoundMarker(parcel);
        }

        public CompoundMarker[] newArray(int size) {
            return new CompoundMarker[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(index);
        parcel.writeString(type);
        parcel.writeString(label);
    }
}
