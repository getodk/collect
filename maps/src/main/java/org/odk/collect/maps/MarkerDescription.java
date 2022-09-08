package org.odk.collect.maps;

public class MarkerDescription {

    private final MapPoint point;
    private final boolean draggable;
    private final String iconAnchor;
    private final int iconDrawableId;

    public MarkerDescription(MapPoint point, boolean draggable, @MapFragment.IconAnchor String iconAnchor, int iconDrawableId) {
        this.point = point;
        this.draggable = draggable;
        this.iconAnchor = iconAnchor;
        this.iconDrawableId = iconDrawableId;
    }

    public MapPoint getPoint() {
        return point;
    }

    public boolean isDraggable() {
        return draggable;
    }

    @MapFragment.IconAnchor
    public String getIconAnchor() {
        return iconAnchor;
    }

    public int getIconDrawableId() {
        return iconDrawableId;
    }
}
